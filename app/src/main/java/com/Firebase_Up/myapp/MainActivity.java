package com.dhautomation.firebasedeploy;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class MainActivity extends Activity {

    private static final int REQ_PICK_FOLDER = 1001;
    private static final int REQ_PICK_JSON = 1002;

    private Uri selectedFolderUri;
    private String siteId = "";
    private String clientEmail = "";
    private String privateKeyPem = "";

    private TextView tvAccountStatus, tvFolderPath, tvLog;
    private ScrollView scrollLog;
    private Button btnImportJson, btnBrowse, btnUpload;
    private SharedPreferences prefs;

    private static class FileItem {
        String webPath;
        String sha256;
        byte[] gzippedData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAccountStatus = findViewById(R.id.tvAccountStatus);
        tvFolderPath = findViewById(R.id.tvFolderPath);
        tvLog = findViewById(R.id.tvLog);
        scrollLog = findViewById(R.id.scrollLog);
        btnImportJson = findViewById(R.id.btnImportJson);
        btnBrowse = findViewById(R.id.btnBrowse);
        btnUpload = findViewById(R.id.btnUpload);

        prefs = getSharedPreferences("dh_firebase_deploy", MODE_PRIVATE);
        loadSavedSettings();

        btnImportJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, REQ_PICK_JSON);
            }
        });

        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(intent, REQ_PICK_FOLDER);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (siteId.isEmpty() || clientEmail.isEmpty() || privateKeyPem.isEmpty()) {
                    log("❌ خطأ: اختار ملف مفاتيح فايربيس (.json) الأول!");
                    return;
                }
                if (selectedFolderUri == null) {
                    log("❌ خطأ: اختار فولدر public الأول!");
                    return;
                }
                startDeployProcess();
            }
        });
    }

    private void loadSavedSettings() {
        siteId = prefs.getString("site_id", "");
        clientEmail = prefs.getString("client_email", "");
        privateKeyPem = prefs.getString("private_key", "");

        if (!siteId.isEmpty() && !clientEmail.isEmpty()) {
            tvAccountStatus.setText("متصل بالمشروع: " + siteId);
            tvAccountStatus.setTextColor(0xFF00E676);
        }

        String savedUri = prefs.getString("folder_uri", null);
        if (savedUri != null) {
            selectedFolderUri = Uri.parse(savedUri);
            tvFolderPath.setText("الفولدر المحفوظ: " + selectedFolderUri.getLastPathSegment());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;

        if (requestCode == REQ_PICK_JSON) {
            try {
                byte[] jsonBytes = readUriBytes(getContentResolver(), data.getData());
                JSONObject obj = new JSONObject(new String(jsonBytes, StandardCharsets.UTF_8));
                siteId = obj.getString("project_id");
                clientEmail = obj.getString("client_email");
                privateKeyPem = obj.getString("private_key");

                prefs.edit()
                        .putString("site_id", siteId)
                        .putString("client_email", clientEmail)
                        .putString("private_key", privateKeyPem)
                        .apply();

                tvAccountStatus.setText("متصل بالمشروع: " + siteId);
                tvAccountStatus.setTextColor(0xFF00E676);
                log("✔ تم حفظ بيانات مشروع (" + siteId + ") بنجاح.");
            } catch (Exception e) {
                log("❌ ملف JSON غير صالح: " + e.getMessage());
            }
        } else if (requestCode == REQ_PICK_FOLDER) {
            selectedFolderUri = data.getData();
            getContentResolver().takePersistableUriPermission(
                    selectedFolderUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            prefs.edit().putString("folder_uri", selectedFolderUri.toString()).apply();
            tvFolderPath.setText("الفولدر المحفوظ: " + selectedFolderUri.getLastPathSegment());
            log("✔ تم حفظ مسار فولدر public بنجاح.");
        }
    }

    private void startDeployProcess() {
        btnUpload.setEnabled(false);
        tvLog.setText("بدء عملية النشر...\n");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    log("1. فحص وضغط الملفات وحساب بصمة SHA-256...");
                    Map<String, FileItem> filesMap = new HashMap<>();
                    String rootDocId = DocumentsContract.getTreeDocumentId(selectedFolderUri);
                    scanFolderRecursive(selectedFolderUri, rootDocId, "", filesMap);
                    log("إجمالي الملفات في الفولدر: " + filesMap.size());

                    log("2. المصادقة مع سيرفر جوجل...");
                    String accessToken = getGoogleAccessToken();

                    log("3. إنشاء إصدار جديد على Firebase Hosting...");
                    String createVerUrl = "https://firebasehosting.googleapis.com/v1beta1/sites/" + siteId + "/versions";
                    String verResp = httpJsonRequest("POST", createVerUrl, accessToken, "{}");
                    String versionName = new JSONObject(verResp).getString("name");

                    log("4. مقارنة البصمات لتحديد الملفات المحدثة فقط...");
                    JSONObject filesJson = new JSONObject();
                    Map<String, FileItem> hashToItem = new HashMap<>();
                    for (FileItem item : filesMap.values()) {
                        filesJson.put(item.webPath, item.sha256);
                        hashToItem.put(item.sha256, item);
                    }
                    JSONObject populateBody = new JSONObject();
                    populateBody.put("files", filesJson);

                    String populateUrl = "https://firebasehosting.googleapis.com/v1beta1/" + versionName + ":populateFiles";
                    String popResp = httpJsonRequest("POST", populateUrl, accessToken, populateBody.toString());
                    JSONObject popObj = new JSONObject(popResp);

                    if (popObj.has("uploadRequiredHashes")) {
                        JSONArray required = popObj.getJSONArray("uploadRequiredHashes");
                        String uploadUrlBase = popObj.getString("uploadUrl");
                        log("ملفات جديدة/معدلة سيتم رفعها: " + required.length());

                        for (int i = 0; i < required.length(); i++) {
                            String hash = required.getString(i);
                            FileItem item = hashToItem.get(hash);
                            if (item != null) {
                                log("⬆ رفع: " + item.webPath);
                                uploadGzippedFile(uploadUrlBase + "/" + hash, accessToken, item.gzippedData);
                            }
                        }
                    } else {
                        log("✔ كل الملفات محدثة بالفعل! لا يوجد جديد للرفع.");
                    }

                    log("5. اعتماد الإصدار ونشره على الموقع...");
                    String finalizeUrl = "https://firebasehosting.googleapis.com/v1beta1/" + versionName + "?updateMask=status";
                    httpJsonRequest("PATCH", finalizeUrl, accessToken, "{\"status\":\"FINALIZED\"}");

                    String releaseUrl = "https://firebasehosting.googleapis.com/v1beta1/sites/" + siteId + "/releases?versionName=" + versionName;
                    httpJsonRequest("POST", releaseUrl, accessToken, "{}");

                    log("\n✅ تم النشر بنجاح على: https://" + siteId + ".web.app");
                } catch (Exception e) {
                    log("\n❌ خطأ: " + e.getMessage());
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnUpload.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }

    private void scanFolderRecursive(Uri treeUri, String parentDocId, String currentPath, Map<String, FileItem> result) throws Exception {
        ContentResolver resolver = getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId);
        Cursor cursor = resolver.query(childrenUri, new String[]{
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
        }, null, null, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String docId = cursor.getString(0);
                    String name = cursor.getString(1);
                    String mime = cursor.getString(2);

                    if (name.startsWith(".")) continue;

                    String nextPath = currentPath + "/" + name;
                    if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                        scanFolderRecursive(treeUri, docId, nextPath, result);
                    } else {
                        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
                        byte[] rawBytes = readUriBytes(resolver, fileUri);
                        byte[] gzBytes = gzipBytes(rawBytes);
                        String hash = sha256Hex(gzBytes);

                        FileItem item = new FileItem();
                        item.webPath = nextPath;
                        item.gzippedData = gzBytes;
                        item.sha256 = hash;
                        result.put(nextPath, item);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    private String getGoogleAccessToken() throws Exception {
        long now = System.currentTimeMillis() / 1000L;
        JSONObject header = new JSONObject();
        header.put("alg", "RS256");
        header.put("typ", "JWT");

        JSONObject claim = new JSONObject();
        claim.put("iss", clientEmail);
        claim.put("scope", "https://www.googleapis.com/auth/firebase.hosting");
        claim.put("aud", "https://oauth2.googleapis.com/token");
        claim.put("iat", now);
        claim.put("exp", now + 3600);

        String unsignedJwt = base64Url(header.toString().getBytes(StandardCharsets.UTF_8)) + "." +
                             base64Url(claim.toString().getBytes(StandardCharsets.UTF_8));

        String cleanKey = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\n", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.decode(cleanKey, Base64.DEFAULT);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(unsignedJwt.getBytes(StandardCharsets.UTF_8));
        String jwt = unsignedJwt + "." + base64Url(signature.sign());

        String postData = "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", "UTF-8") +
                          "&assertion=" + URLEncoder.encode(jwt, "UTF-8");

        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = readStreamString(is);
        if (code < 200 || code >= 300) {
            throw new Exception("فشل المصادقة: " + resp);
        }
        return new JSONObject(resp).getString("access_token");
    }

    private String httpJsonRequest(String method, String urlStr, String token, String jsonBody) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if ("PATCH".equals(method)) {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        } else {
            conn.setRequestMethod(method);
        }
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = readStreamString(is);
        if (code < 200 || code >= 300) {
            throw new Exception("HTTP " + code + ": " + resp);
        }
        return resp;
    }

    private void uploadGzippedFile(String urlStr, String token, byte[] gzData) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(gzData);
        }

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new Exception("فشل رفع الملف (" + code + "): " + readStreamString(conn.getErrorStream()));
        }
    }

    private byte[] readUriBytes(ContentResolver resolver, Uri uri) throws Exception {
        try (InputStream is = resolver.openInputStream(uri);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            return bos.toByteArray();
        }
    }

    private byte[] gzipBytes(byte[] raw) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gos = new GZIPOutputStream(bos)) {
            gos.write(raw);
        }
        return bos.toByteArray();
    }

    private String sha256Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String base64Url(byte[] data) {
        return Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    private String readStreamString(InputStream is) throws Exception {
        if (is == null) return "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toString("UTF-8");
    }

    private void log(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLog.append("\n" + msg);
                scrollLog.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollLog.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }
}
