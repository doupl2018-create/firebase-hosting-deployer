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
import android.widget.EditText;
import android.widget.LinearLayout;
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

    private static final int REQ_PICK_FOLDER_FB = 1001;
    private static final int REQ_PICK_JSON_FB = 1002;
    private static final int REQ_PICK_FOLDER_GH = 1003;
    private static final int REQ_PICK_FOLDER_DRIVE = 1004;

    private Uri folderFb;
    private String siteId = "";
    private String clientEmailFb = "";
    private String privateKeyFb = "";
    private TextView tvAccountStatusFb, tvFolderPathFb, tvLogFb;
    private ScrollView scrollLogFb;
    private Button btnImportJsonFb, btnBrowseFb, btnUploadFb;

    private Uri folderGh;
    private String ghToken = "";
    private String ghRepo = "";
    private String ghBranch = "main";
    private TextView tvGhStatus, tvFolderPathGh, tvLogGh;
    private EditText etGhToken, etGhRepo, etGhBranch;
    private ScrollView scrollLogGh;
    private Button btnSaveGh, btnBrowseGh, btnUploadGh;

    private Uri folderDrive;
    private String driveFolderId = "";
    private String driveAccessToken = "";
    private String driveRefreshToken = "";
    private String driveClientId = "";
    private String driveClientSecret = "";
    private TextView tvAccountStatusDrive, tvFolderPathDrive, tvLogDrive;
    private EditText etDriveFolderId, etDriveClientId, etDriveClientSecret, etDriveAuthCode;
    private ScrollView scrollLogDrive;
    private Button btnGetDriveCode, btnExchangeDriveCode, btnSaveDrive, btnBrowseDrive, btnUploadDrive;

    private LinearLayout layoutFirebase, layoutGh, layoutDrive;
    private Button tabFirebase, tabGh, tabDrive;

    private SharedPreferences prefs;

    private static class FileItem {
        String webPath;
        String sha256;
        byte[] gzippedData;
        byte[] rawData;
    }

    private static class GHFileInfo {
        String sha;
        String contentBase64;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("dh_firebase_deploy", MODE_PRIVATE);

        tabFirebase = findViewById(R.id.tabFirebase);
        tabGh = findViewById(R.id.tabGh);
        tabDrive = findViewById(R.id.tabDrive);
        layoutFirebase = findViewById(R.id.layoutFirebase);
        layoutGh = findViewById(R.id.layoutGh);
        layoutDrive = findViewById(R.id.layoutDrive);

        tabFirebase.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTab(0); }
        });
        tabGh.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTab(1); }
        });
        tabDrive.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTab(2); }
        });

        tvAccountStatusFb = findViewById(R.id.tvAccountStatusFb);
        tvFolderPathFb = findViewById(R.id.tvFolderPathFb);
        tvLogFb = findViewById(R.id.tvLogFb);
        scrollLogFb = findViewById(R.id.scrollLogFb);
        btnImportJsonFb = findViewById(R.id.btnImportJsonFb);
        btnBrowseFb = findViewById(R.id.btnBrowseFb);
        btnUploadFb = findViewById(R.id.btnUploadFb);

        etGhToken = findViewById(R.id.etGhToken);
        etGhRepo = findViewById(R.id.etGhRepo);
        etGhBranch = findViewById(R.id.etGhBranch);
        tvGhStatus = findViewById(R.id.tvGhStatus);
        tvFolderPathGh = findViewById(R.id.tvFolderPathGh);
        tvLogGh = findViewById(R.id.tvLogGh);
        scrollLogGh = findViewById(R.id.scrollLogGh);
        btnSaveGh = findViewById(R.id.btnSaveGh);
        btnBrowseGh = findViewById(R.id.btnBrowseGh);
        btnUploadGh = findViewById(R.id.btnUploadGh);

        tvAccountStatusDrive = findViewById(R.id.tvAccountStatusDrive);
        tvFolderPathDrive = findViewById(R.id.tvFolderPathDrive);
        tvLogDrive = findViewById(R.id.tvLogDrive);
        scrollLogDrive = findViewById(R.id.scrollLogDrive);
        etDriveFolderId = findViewById(R.id.etDriveFolderId);
        etDriveClientId = findViewById(R.id.etDriveClientId);
        etDriveClientSecret = findViewById(R.id.etDriveClientSecret);
        etDriveAuthCode = findViewById(R.id.etDriveAuthCode);
        btnGetDriveCode = findViewById(R.id.btnGetDriveCode);
        btnExchangeDriveCode = findViewById(R.id.btnExchangeDriveCode);
        btnSaveDrive = findViewById(R.id.btnSaveDrive);
        btnBrowseDrive = findViewById(R.id.btnBrowseDrive);
        btnUploadDrive = findViewById(R.id.btnUploadDrive);

        loadSavedSettings();

        btnImportJsonFb.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, REQ_PICK_JSON_FB);
            }
        });
        btnBrowseFb.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(intent, REQ_PICK_FOLDER_FB);
            }
        });
        btnUploadFb.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (siteId.isEmpty() || clientEmailFb.isEmpty() || privateKeyFb.isEmpty()) { logFb("اختار ملف مفاتيح فايربيس الاول!"); return; }
                if (folderFb == null) { logFb("اختار فولدر فايربيس الاول!"); return; }
                startDeployProcess();
            }
        });

        btnSaveGh.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ghToken = etGhToken.getText().toString().trim();
                ghRepo = normalizeGhRepo(etGhRepo.getText().toString().trim());
                String b = etGhBranch.getText().toString().trim();
                if (!b.isEmpty()) ghBranch = b; else ghBranch = "main";
                if (ghToken.isEmpty() || ghRepo.isEmpty() || !ghRepo.contains("/")) { logGh("بيانات GH غير كاملة"); return; }
                etGhRepo.setText(ghRepo);
                prefs.edit().putString("gh_token", ghToken).putString("gh_repo", ghRepo).putString("gh_branch", ghBranch).apply();
                tvGhStatus.setText("متصل بـ GH: " + ghRepo + " (" + ghBranch + ")");
                tvGhStatus.setTextColor(0xFF00E676);
                logGh("تم حفظ بيانات GH: " + ghRepo);
            }
        });
        btnBrowseGh.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(intent, REQ_PICK_FOLDER_GH);
            }
        });
        btnUploadGh.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (ghToken.isEmpty() || ghRepo.isEmpty()) { logGh("احفظ بيانات GH الاول!"); return; }
                if (folderGh == null) { logGh("اختار فولدر GH الاول!"); return; }
                startGhUpload();
            }
        });

        btnGetDriveCode.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String cid = etDriveClientId.getText().toString().trim();
                if (cid.isEmpty()) { logDrive("حط Client ID الاول"); return; }
                prefs.edit().putString("drive_client_id", cid).putString("drive_client_secret", etDriveClientSecret.getText().toString().trim()).apply();
                String url = "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + cid + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob&response_type=code&scope=https://www.googleapis.com/auth/drive.file&access_type=offline&prompt=select_account";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
                logDrive("اختار ايميلك ووافق، وبعدين انسخ الكود والصقه هنا");
            }
        });
        btnExchangeDriveCode.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final String code = etDriveAuthCode.getText().toString().trim();
                final String cid = etDriveClientId.getText().toString().trim();
                final String secret = etDriveClientSecret.getText().toString().trim();
                if (code.isEmpty() || cid.isEmpty() || secret.isEmpty()) { logDrive("كمل الـ 3 خانات الاول"); return; }
                new Thread(new Runnable() {
                    @Override public void run() {
                        try {
                            logDrive("جارى تحويل الكود لتوكن...");
                            String post = "code=" + URLEncoder.encode(code, "UTF-8") + "&client_id=" + URLEncoder.encode(cid, "UTF-8") + "&client_secret=" + URLEncoder.encode(secret, "UTF-8") + "&redirect_uri=" + URLEncoder.encode("urn:ietf:wg:oauth:2.0:oob", "UTF-8") + "&grant_type=authorization_code";
                            URL url = new URL("https://oauth2.googleapis.com/token");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            conn.setDoOutput(true);
                            OutputStream os = conn.getOutputStream();
                            try { os.write(post.getBytes(StandardCharsets.UTF_8)); } finally { os.close(); }
                            int c = conn.getResponseCode();
                            String resp = readStreamString(c >= 200 && c < 300 ? conn.getInputStream() : conn.getErrorStream());
                            if (c < 200 || c >= 300) throw new Exception(resp);
                            JSONObject o = new JSONObject(resp);
                            driveAccessToken = o.getString("access_token");
                            if (o.has("refresh_token")) driveRefreshToken = o.getString("refresh_token");
                            prefs.edit().putString("drive_access_token", driveAccessToken).putString("drive_refresh_token", driveRefreshToken).putString("drive_client_id", cid).putString("drive_client_secret", secret).apply();
                            logDrive("تم تسجيل الدخول بحسابك بنجاح، دلوقتي دوس رفع");
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    tvAccountStatusDrive.setText("متصل بحسابك الشخصي");
                                    tvAccountStatusDrive.setTextColor(0xFF00E676);
                                }
                            });
                        } catch (Exception e) { logDrive("فشل تسجيل الدخول: " + e.getMessage()); }
                    }
                }).start();
            }
        });
        btnSaveDrive.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                driveFolderId = extractDriveFolderId(etDriveFolderId.getText().toString().trim());
                if (driveFolderId.isEmpty()) { logDrive("اكتب ID فولدر Drive الاول"); return; }
                etDriveFolderId.setText(driveFolderId);
                prefs.edit().putString("drive_folder_id", driveFolderId).apply();
                logDrive("تم حفظ ID فولدر Drive: " + driveFolderId);
            }
        });
        btnBrowseDrive.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(intent, REQ_PICK_FOLDER_DRIVE);
            }
        });
        btnUploadDrive.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (driveAccessToken.isEmpty()) { logDrive("سجل دخول الاول"); return; }
                if (driveFolderId.isEmpty()) { logDrive("احفظ ID فولدر Drive الاول!"); return; }
                if (folderDrive == null) { logDrive("اختار فولدر Drive الاول!"); return; }
                startDriveUpload();
            }
        });

        showTab(0);
    }

    private void showTab(int tab) {
        layoutFirebase.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        layoutGh.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);
        layoutDrive.setVisibility(tab == 2 ? View.VISIBLE : View.GONE);
        tabFirebase.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tab == 0 ? 0xFF00C853 : 0xFF37474F));
        tabGh.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tab == 1 ? 0xFF00C853 : 0xFF37474F));
        tabDrive.setBackgroundTintList(android.content.res.ColorStateList.valueOf(tab == 2 ? 0xFF00C853 : 0xFF37474F));
        tabFirebase.setTextColor(tab == 0 ? 0xFF000000 : 0xFFFFFFFF);
        tabGh.setTextColor(tab == 1 ? 0xFF000000 : 0xFFFFFFFF);
        tabDrive.setTextColor(tab == 2 ? 0xFF000000 : 0xFFFFFFFF);
        if (tab == 0) scrollLogFb.post(new Runnable() { @Override public void run() { scrollLogFb.fullScroll(View.FOCUS_DOWN); } });
        else if (tab == 1) scrollLogGh.post(new Runnable() { @Override public void run() { scrollLogGh.fullScroll(View.FOCUS_DOWN); } });
        else if (tab == 2) scrollLogDrive.post(new Runnable() { @Override public void run() { scrollLogDrive.fullScroll(View.FOCUS_DOWN); } });
    }

    private String normalizeGhRepo(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.endsWith(".git")) s = s.substring(0, s.length() - 4);
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        if (s.contains(" github.com/")) s = s.substring(s.indexOf(" github.com/") + " github.com/".length());
        else if (s.contains(" github.com:")) s = s.substring(s.indexOf(" github.com:") + " github.com:".length());
        String[] parts = s.split("/");
        if (parts.length >= 2) s = parts[0] + "/" + parts[1];
        return s.trim();
    }

    private String extractDriveFolderId(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.contains("folders/")) {
            s = s.substring(s.indexOf("folders/") + "folders/".length());
            int q = s.indexOf("?");
            if (q >= 0) s = s.substring(0, q);
            int slash = s.indexOf("/");
            if (slash >= 0) s = s.substring(0, slash);
        }
        return s.trim();
    }

    private void loadSavedSettings() {
        siteId = prefs.getString("fb_site_id", "");
        clientEmailFb = prefs.getString("fb_client_email", "");
        privateKeyFb = prefs.getString("fb_private_key", "");
        if (!siteId.isEmpty() && !clientEmailFb.isEmpty()) {
            tvAccountStatusFb.setText("متصل بالمشروع: " + siteId);
            tvAccountStatusFb.setTextColor(0xFF00E676);
        }
        String uriFb = prefs.getString("fb_folder_uri", null);
        if (uriFb != null) { folderFb = Uri.parse(uriFb); tvFolderPathFb.setText("الفولدر المحفوظ: " + folderFb.getLastPathSegment()); }
        ghToken = prefs.getString("gh_token", "");
        ghRepo = normalizeGhRepo(prefs.getString("gh_repo", ""));
        ghBranch = prefs.getString("gh_branch", "main");
        if (!ghToken.isEmpty()) etGhToken.setText(ghToken);
        if (!ghRepo.isEmpty()) etGhRepo.setText(ghRepo);
        if (ghBranch != null && !ghBranch.isEmpty()) etGhBranch.setText(ghBranch);
        if (!ghToken.isEmpty() && !ghRepo.isEmpty()) {
            tvGhStatus.setText("متصل بـ GH: " + ghRepo + " (" + ghBranch + ")");
            tvGhStatus.setTextColor(0xFF00E676);
        }
        String uriGh = prefs.getString("gh_folder_uri", null);
        if (uriGh != null) { folderGh = Uri.parse(uriGh); tvFolderPathGh.setText("الفولدر المحفوظ: " + folderGh.getLastPathSegment()); }
        driveClientId = prefs.getString("drive_client_id", "");
        driveClientSecret = prefs.getString("drive_client_secret", "");
        driveAccessToken = prefs.getString("drive_access_token", "");
        driveRefreshToken = prefs.getString("drive_refresh_token", "");
        driveFolderId = prefs.getString("drive_folder_id", "");
        if (!driveClientId.isEmpty()) etDriveClientId.setText(driveClientId);
        if (!driveClientSecret.isEmpty()) etDriveClientSecret.setText(driveClientSecret);
        if (!driveAccessToken.isEmpty()) {
            tvAccountStatusDrive.setText("متصل بحسابك الشخصي");
            tvAccountStatusDrive.setTextColor(0xFF00E676);
        }
        if (!driveFolderId.isEmpty()) etDriveFolderId.setText(driveFolderId);
        String uriDrive = prefs.getString("drive_folder_uri", null);
        if (uriDrive != null) { folderDrive = Uri.parse(uriDrive); tvFolderPathDrive.setText("الفولدر المحفوظ: " + folderDrive.getLastPathSegment()); }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        try {
            if (requestCode == REQ_PICK_JSON_FB) {
                byte[] jsonBytes = readUriBytes(getContentResolver(), data.getData());
                JSONObject obj = new JSONObject(new String(jsonBytes, StandardCharsets.UTF_8));
                siteId = obj.getString("project_id");
                clientEmailFb = obj.getString("client_email");
                privateKeyFb = obj.getString("private_key");
                prefs.edit().putString("fb_site_id", siteId).putString("fb_client_email", clientEmailFb).putString("fb_private_key", privateKeyFb).apply();
                tvAccountStatusFb.setText("متصل بالمشروع: " + siteId);
                tvAccountStatusFb.setTextColor(0xFF00E676);
                logFb("تم حفظ بيانات مشروع (" + siteId + ") بنجاح.");
            } else if (requestCode == REQ_PICK_FOLDER_FB) {
                folderFb = data.getData();
                getContentResolver().takePersistableUriPermission(folderFb, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                prefs.edit().putString("fb_folder_uri", folderFb.toString()).apply();
                tvFolderPathFb.setText("الفولدر المحفوظ: " + folderFb.getLastPathSegment());
                logFb("تم حفظ مسار فولدر Firebase بنجاح.");
            } else if (requestCode == REQ_PICK_FOLDER_GH) {
                folderGh = data.getData();
                getContentResolver().takePersistableUriPermission(folderGh, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                prefs.edit().putString("gh_folder_uri", folderGh.toString()).apply();
                tvFolderPathGh.setText("الفولدر المحفوظ: " + folderGh.getLastPathSegment());
                logGh("تم حفظ مسار فولدر GH بنجاح.");
            } else if (requestCode == REQ_PICK_FOLDER_DRIVE) {
                folderDrive = data.getData();
                getContentResolver().takePersistableUriPermission(folderDrive, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                prefs.edit().putString("drive_folder_uri", folderDrive.toString()).apply();
                tvFolderPathDrive.setText("الفولدر المحفوظ: " + folderDrive.getLastPathSegment());
                logDrive("تم حفظ مسار فولدر Drive بنجاح.");
            }
        } catch (Exception e) { logFb("خطأ: " + e.getMessage()); }
    }

    private void startDeployProcess() {
        btnUploadFb.setEnabled(false);
        tvLogFb.setText("بدء عملية النشر...\n");
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    logFb("1. فحص وضغط الملفات...");
                    Map<String, FileItem> filesMap = new HashMap<String, FileItem>();
                    String rootDocId = DocumentsContract.getTreeDocumentId(folderFb);
                    scanFolderRecursive(folderFb, rootDocId, "", filesMap);
                    logFb("اجمالي الملفات: " + filesMap.size());
                    logFb("2. المصادقة...");
                    String accessToken = getGoogleAccessToken(clientEmailFb, privateKeyFb, "https://www.googleapis.com/auth/firebase.hosting");
                    logFb("3. انشاء اصدار جديد...");
                    String createVerUrl = "https://firebasehosting.googleapis.com/v1beta1/sites/" + siteId + "/versions";
                    String verResp = httpJsonRequest("POST", createVerUrl, accessToken, "{}");
                    String versionName = new JSONObject(verResp).getString("name");
                    logFb("4. مقارنة البصمات...");
                    JSONObject filesJson = new JSONObject();
                    Map<String, FileItem> hashToItem = new HashMap<String, FileItem>();
                    for (FileItem item : filesMap.values()) { filesJson.put(item.webPath, item.sha256); hashToItem.put(item.sha256, item); }
                    JSONObject populateBody = new JSONObject(); populateBody.put("files", filesJson);
                    String populateUrl = "https://firebasehosting.googleapis.com/v1beta1/" + versionName + ":populateFiles";
                    String popResp = httpJsonRequest("POST", populateUrl, accessToken, populateBody.toString());
                    JSONObject popObj = new JSONObject(popResp);
                    if (popObj.has("uploadRequiredHashes")) {
                        JSONArray required = popObj.getJSONArray("uploadRequiredHashes");
                        String uploadUrlBase = popObj.getString("uploadUrl");
                        logFb("ملفات جديدة/معدلة: " + required.length());
                        for (int i = 0; i < required.length(); i++) {
                            String hash = required.getString(i);
                            FileItem item = hashToItem.get(hash);
                            if (item != null) { logFb("رفع: " + item.webPath); uploadGzippedFile(uploadUrlBase + "/" + hash, accessToken, item.gzippedData); }
                        }
                    } else logFb("كل الملفات محدثة بالفعل!");
                    logFb("5. اعتماد الاصدار...");
                    String finalizeUrl = "https://firebasehosting.googleapis.com/v1beta1/" + versionName + "?updateMask=status";
                    httpJsonRequest("PATCH", finalizeUrl, accessToken, "{\"status\":\"FINALIZED\"}");
                    String releaseUrl = "https://firebasehosting.googleapis.com/v1beta1/sites/" + siteId + "/releases?versionName=" + versionName;
                    httpJsonRequest("POST", releaseUrl, accessToken, "{}");
                    logFb("تم النشر بنجاح على: https://" + siteId + ".web.app");
                } catch (Exception e) { logFb("خطأ: " + e.getMessage()); }
                finally { runOnUiThread(new Runnable() { @Override public void run() { btnUploadFb.setEnabled(true); } }); }
            }
        }).start();
    }

    private void startGhUpload() {
        btnUploadGh.setEnabled(false);
        tvLogGh.setText("بدء الرفع على GH...\n");
        new Thread(new Runnable() {
            @Override public void run() {
                int uploaded = 0; int skipped = 0; int failed = 0;
                try {
                    logGh("1. قراءة ملفات الفولدر...");
                    Map<String, FileItem> filesMap = new HashMap<String, FileItem>();
                    String rootDocId = DocumentsContract.getTreeDocumentId(folderGh);
                    scanFolderRecursive(folderGh, rootDocId, "", filesMap);
                    logGh("اجمالي الملفات: " + filesMap.size());
                    if (filesMap.isEmpty()) { logGh("لا يوجد ملفات!"); logGh("===== لم يتم رفع اي ملف ====="); return; }
                    logGh("2. مقارنة ورفع المتعدل بس...");
                    int count = 0;
                    for (Map.Entry<String, FileItem> entry : filesMap.entrySet()) {
                        String ghPath = entry.getValue().webPath.startsWith("/") ? entry.getValue().webPath.substring(1) : entry.getValue().webPath;
                        count++;
                        try {
                            boolean did = uploadOneFileToGhIfChanged(ghPath, entry.getValue().rawData, count, filesMap.size());
                            if (did) uploaded++; else skipped++;
                        } catch (Exception oneErr) { failed++; logGh("فشل في ملف: " + ghPath + " - " + oneErr.getMessage()); }
                    }
                    logGh("--------------------------------");
                    logGh("خلصنا GH");
                    logGh("اترفع: " + uploaded);
                    logGh("اتخطى: " + skipped);
                    logGh("فشل: " + failed);
                    if (failed == 0) logGh("===== تم الرفع بنجاح بالكامل =====");
                    else logGh("===== خلص بس فيه " + failed + " ملف فشل =====");
                } catch (Exception e) { logGh("خطأ عام في GH: " + e.getMessage()); logGh("===== الرفع لم يكتمل ====="); }
                finally {
                    final int u = uploaded; final int s = skipped; final int f = failed;
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            btnUploadGh.setEnabled(true);
                            android.widget.Toast.makeText(MainActivity.this, "خلصنا GH: اترفع " + u + " - اتخطى " + s + " - فشل " + f, android.widget.Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private boolean uploadOneFileToGhIfChanged(String ghPath, byte[] rawData, int index, int total) throws Exception {
        String encodedPath = encodeGhPath(ghPath);
        String urlStr = "https://api.github.com/repos/" + ghRepo + "/contents/" + encodedPath;
        GHFileInfo old = getGhFileInfo(urlStr);
        String newBase64 = Base64.encodeToString(rawData, Base64.NO_WRAP);
        if (old != null && old.contentBase64 != null) {
            String oldClean = old.contentBase64.replaceAll("\\s+", "");
            if (oldClean.equals(newBase64)) { logGh("تخطي: " + ghPath); return false; }
            logGh("تحديث: " + ghPath);
            doPutGhFile(urlStr, ghPath, newBase64, old.sha);
            return true;
        } else {
            logGh("جديد: " + ghPath);
            doPutGhFile(urlStr, ghPath, newBase64, null);
            return true;
        }
    }

    private void doPutGhFile(String urlStr, String ghPath, String base64Content, String sha) throws Exception {
        JSONObject body = new JSONObject();
        body.put("message", "Update " + ghPath + " via DH Deployer");
        body.put("content", base64Content);
        body.put("branch", ghBranch);
        if (sha != null) body.put("sha", sha);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "Bearer " + ghToken);
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        try { os.write(body.toString().getBytes(StandardCharsets.UTF_8)); } finally { os.close(); }
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = readStreamString(is);
        if (code < 200 || code >= 300) throw new Exception("فشل رفع " + ghPath + " (" + code + "): " + resp);
    }

    private GHFileInfo getGhFileInfo(String urlStr) {
        try {
            URL url = new URL(urlStr + "?ref=" + URLEncoder.encode(ghBranch, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + ghToken);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            int code = conn.getResponseCode();
            if (code == 200) {
                String resp = readStreamString(conn.getInputStream());
                JSONObject obj = new JSONObject(resp);
                GHFileInfo info = new GHFileInfo();
                info.sha = obj.optString("sha", null);
                info.contentBase64 = obj.optString("content", null);
                return info;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String encodeGhPath(String path) throws Exception {
        String[] parts = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("/");
            sb.append(URLEncoder.encode(parts[i], "UTF-8").replace("+", "%20"));
        }
        return sb.toString();
    }

    private String getValidDriveToken() throws Exception {
        try {
            URL url = new URL("https://www.googleapis.com/drive/v3/files?pageSize=1&fields=files(id)");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + driveAccessToken);
            int code = conn.getResponseCode();
            if (code == 200) return driveAccessToken;
        } catch (Exception ignored) {}
        if (driveRefreshToken.isEmpty()) return driveAccessToken;
        logDrive("التوكن انتهى، جارى تجديده...");
        String post = "client_id=" + URLEncoder.encode(driveClientId, "UTF-8") + "&client_secret=" + URLEncoder.encode(driveClientSecret, "UTF-8") + "&refresh_token=" + URLEncoder.encode(driveRefreshToken, "UTF-8") + "&grant_type=refresh_token";
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        try { os.write(post.getBytes(StandardCharsets.UTF_8)); } finally { os.close(); }
        int c = conn.getResponseCode();
        String resp = readStreamString(c >= 200 && c < 300 ? conn.getInputStream() : conn.getErrorStream());
        if (c < 200 || c >= 300) throw new Exception("فشل تجديد التوكن: " + resp);
        JSONObject o = new JSONObject(resp);
        driveAccessToken = o.getString("access_token");
        prefs.edit().putString("drive_access_token", driveAccessToken).apply();
        logDrive("تم تجديد التوكن بنجاح");
        return driveAccessToken;
    }

    private void startDriveUpload() {
        btnUploadDrive.setEnabled(false);
        tvLogDrive.setText("بدء الرفع على Drive...\n");
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    if (driveAccessToken.isEmpty()) { logDrive("سجل دخول الاول"); return; }
                    logDrive("1. قراءة ملفات الفولدر...");
                    Map<String, FileItem> filesMap = new HashMap<String, FileItem>();
                    String rootDocId = DocumentsContract.getTreeDocumentId(folderDrive);
                    scanFolderRecursive(folderDrive, rootDocId, "", filesMap);
                    logDrive("اجمالي الملفات: " + filesMap.size());
                    String token = getValidDriveToken();
                    logDrive("2. التأكد من التوكن...");
                    logDrive("3. جلب ملفات Drive الموجودة...");
                    Map<String, JSONObject> existing = listDriveFiles(token, driveFolderId);
                    logDrive("موجود على Drive: " + existing.size());
                    int uploaded = 0; int skipped = 0; int count = 0;
                    for (Map.Entry<String, FileItem> entry : filesMap.entrySet()) {
                        String name = entry.getValue().webPath.startsWith("/") ? entry.getValue().webPath.substring(1) : entry.getValue().webPath;
                        String driveName = name.replace("/", "__");
                        count++;
                        String localMd5 = md5Hex(entry.getValue().rawData);
                        JSONObject old = existing.get(driveName);
                        if (old != null) {
                            String oldMd5 = old.optString("md5Checksum", "");
                            if (oldMd5.equals(localMd5)) { logDrive("تخطي: " + name); skipped++; continue; }
                            else { logDrive("تحديث: " + name); updateDriveFile(token, old.getString("id"), entry.getValue().rawData, driveName); uploaded++; continue; }
                        }
                        logDrive("جديد: " + name);
                        createDriveFile(token, driveFolderId, entry.getValue().rawData, driveName);
                        uploaded++;
                    }
                    logDrive("خلصنا Drive: اترفع " + uploaded + " - اتخطى " + skipped);
                    logDrive("===== تم الرفع بنجاح =====");
                } catch (Exception e) { logDrive("خطأ Drive: " + e.getMessage()); }
                finally { runOnUiThread(new Runnable() { @Override public void run() { btnUploadDrive.setEnabled(true); } }); }
            }
        }).start();
    }

    private Map<String, JSONObject> listDriveFiles(String token, String folderId) throws Exception {
        Map<String, JSONObject> map = new HashMap<String, JSONObject>();
        String q = URLEncoder.encode("'" + folderId + "' in parents and trashed=false", "UTF-8");
        String urlStr = "https://www.googleapis.com/drive/v3/files?q=" + q + "&fields=files(id,name,md5Checksum)&pageSize=1000";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        int code = conn.getResponseCode();
        String resp = readStreamString(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
        if (code < 200 || code >= 300) throw new Exception("فشل جلب ملفات Drive: " + resp);
        JSONObject obj = new JSONObject(resp);
        if (obj.has("files")) {
            JSONArray arr = obj.getJSONArray("files");
            for (int i = 0; i < arr.length(); i++) { JSONObject f = arr.getJSONObject(i); map.put(f.getString("name"), f); }
        }
        return map;
    }

    private void createDriveFile(String token, String folderId, byte[] data, String fileName) throws Exception {
        String boundary = "DH_BOUNDARY_" + System.currentTimeMillis();
        String meta = "{\"name\":\"" + fileName.replace("\"", "") + "\",\"parents\":[\"" + folderId + "\"]}";
        URL url = new URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "multipart/related; boundary=" + boundary);
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        try {
            String part1 = "--" + boundary + "\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n" + meta + "\r\n--" + boundary + "\r\nContent-Type: application/octet-stream\r\n\r\n";
            os.write(part1.getBytes(StandardCharsets.UTF_8));
            os.write(data);
            os.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        } finally { os.close(); }
        int code = conn.getResponseCode();
        String resp = readStreamString(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
        if (code < 200 || code >= 300) throw new Exception("فشل انشاء ملف Drive " + fileName + ": " + resp);
    }

    private void updateDriveFile(String token, String fileId, byte[] data, String fileName) throws Exception {
        URL url = new URL("https://www.googleapis.com/upload/drive/v3/files/" + fileId + "?uploadType=media");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        try { os.write(data); } finally { os.close(); }
        int code = conn.getResponseCode();
        String resp = readStreamString(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
        if (code < 200 || code >= 300) throw new Exception("فشل تحديث ملف Drive " + fileName + ": " + resp);
    }

    private void scanFolderRecursive(Uri treeUri, String parentDocId, String currentPath, Map<String, FileItem> result) throws Exception {
        ContentResolver resolver = getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId);
        Cursor cursor = resolver.query(childrenUri, new String[]{ DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE }, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String docId = cursor.getString(0);
                    String name = cursor.getString(1);
                    String mime = cursor.getString(2);
                    if (name.startsWith(".")) continue;
                    String nextPath = currentPath + "/" + name;
                    if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) scanFolderRecursive(treeUri, docId, nextPath, result);
                    else {
                        Uri fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
                        byte[] rawBytes = readUriBytes(resolver, fileUri);
                        byte[] gzBytes = gzipBytes(rawBytes);
                        String hash = sha256Hex(gzBytes);
                        FileItem item = new FileItem();
                        item.webPath = nextPath;
                        item.gzippedData = gzBytes;
                        item.rawData = rawBytes;
                        item.sha256 = hash;
                        result.put(nextPath, item);
                    }
                }
            } finally { cursor.close(); }
        }
    }

    private String getGoogleAccessToken(String clientEmail, String privateKeyPem, String scope) throws Exception {
        long now = System.currentTimeMillis() / 1000L;
        JSONObject header = new JSONObject(); header.put("alg", "RS256"); header.put("typ", "JWT");
        JSONObject claim = new JSONObject();
        claim.put("iss", clientEmail); claim.put("scope", scope);
        claim.put("aud", "https://oauth2.googleapis.com/token");
        claim.put("iat", now); claim.put("exp", now + 3600);
        String unsignedJwt = base64Url(header.toString().getBytes(StandardCharsets.UTF_8)) + "." + base64Url(claim.toString().getBytes(StandardCharsets.UTF_8));
        String cleanKey = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replace("\\n", "").replaceAll("\\s+", "");
        byte[] keyBytes = Base64.decode(cleanKey, Base64.DEFAULT);
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(unsignedJwt.getBytes(StandardCharsets.UTF_8));
        String jwt = unsignedJwt + "." + base64Url(signature.sign());
        String postData = "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", "UTF-8") + "&assertion=" + URLEncoder.encode(jwt, "UTF-8");
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        try { os.write(postData.getBytes(StandardCharsets.UTF_8)); } finally { os.close(); }
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = readStreamString(is);
        if (code < 200 || code >= 300) throw new Exception("فشل المصادقة: " + resp);
        return new JSONObject(resp).getString("access_token");
    }

    private String httpJsonRequest(String method, String urlStr, String token, String jsonBody) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if ("PATCH".equals(method)) { conn.setRequestMethod("POST"); conn.setRequestProperty("X-HTTP-Method-Override", "PATCH"); }
        else conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        try { os.write(jsonBody.getBytes(StandardCharsets.UTF_8)); } finally { os.close(); }
        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = readStreamString(is);
        if (code < 200 || code >= 300) throw new Exception("HTTP " + code + ": " + resp);
        return resp;
    }

    private void uploadGzippedFile(String urlStr, String token, byte[] gzData) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        try { os.write(gzData); } finally { os.close(); }
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) throw new Exception("فشل رفع الملف (" + code + "): " + readStreamString(conn.getErrorStream()));
    }

    private byte[] readUriBytes(ContentResolver resolver, Uri uri) throws Exception {
        InputStream is = resolver.openInputStream(uri);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[8192]; int n;
            while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
            return bos.toByteArray();
        } finally { is.close(); bos.close(); }
    }

    private byte[] gzipBytes(byte[] raw) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        try { gos.write(raw); } finally { gos.close(); }
        return bos.toByteArray();
    }

    private String sha256Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String md5Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String base64Url(byte[] data) { return Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING); }

    private String readStreamString(InputStream is) throws Exception {
        if (is == null) return "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int n;
        while ((n = is.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toString("UTF-8");
    }

    private void logFb(final String msg) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                tvLogFb.append("\n" + msg);
                scrollLogFb.post(new Runnable() { @Override public void run() { scrollLogFb.fullScroll(View.FOCUS_DOWN); } });
            }
        });
    }
    private void logGh(final String msg) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                tvLogGh.append("\n" + msg);
                scrollLogGh.post(new Runnable() { @Override public void run() { scrollLogGh.fullScroll(View.FOCUS_DOWN); } });
            }
        });
    }
    private void logDrive(final String msg) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                tvLogDrive.append("\n" + msg);
                scrollLogDrive.post(new Runnable() { @Override public void run() { scrollLogDrive.fullScroll(View.FOCUS_DOWN); } });
            }
        });
    }
}