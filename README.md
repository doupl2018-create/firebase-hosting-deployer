<p align="center">
  <img src="https://img.shields.io/badge/DH%20Trinity-v1.1-green?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/3%20in%201-Firebase%20%7C%20GH%20%7C%20Drive-blue?style=for-the-badge" />
</p>

<h1 align="center">DH Trinity v1.1</h1>
<h3 align="center">3 in 1 Deployer</h3>
<p align="center">برنامج أندرويد واحد | One 【entity-Android¦canonical_name=Android】 App</p>
<p align="center"><b>Firebase Hosting + GH + Google Drive</b></p>

<p align="center">
  <a href="https://github.com/doupl2018-create/firebase-hosting-deployer/releases/tag/v1.1-trinity">📥 تحميل آخر نسخة | Download Latest Release</a>
</p>

---

# 🇪🇬 النسخة العربية

## 📥 تحميل البرنامج

حمل ملف: **`DH-Trinity-v1.1.apk`**
من هنا:
https://github.com/doupl2018-create/firebase-hosting-deployer/releases/tag/v1.1-trinity

ثبت الملف على موبايلك، وبعد التثبيت دخل بياناتك جوه البرنامج.
**مفيش أي توكن أو مفتاح متخزن في الكود.**

---

## ✨ المميزات

| التبويب | اللي بيعمله |
| :--- | :--- |
| 🔥 **Firebase** | رفع موقع كامل على Firebase Hosting |
| ⬛ **GH** | رفع فولدر كامل على أي ريبو GH |
| 💙 **Google Drive** | رفع فولدر كامل على أي فولدر Drive بحسابك الشخصي |

### 🔥 تبويب Firebase
- رفع موقع كامل على Firebase Hosting
- رفع الملفات المتعدلة بس عن طريق مقارنة الـ Hash
- ضغط الملفات GZIP قبل الرفع
- تسجيل دخول بملف JSON بتاع Service Account
- كل تبويب ليه فولدره والسجل بتاعه لوحده

### ⬛ تبويب GH
- رفع فولدر كامل على أي ريبو GH
- رفع المتعدل بس، والباقي بيتعمل له Skip
- عداد واضح في الآخر: اترفع / اتخطى / فشل
- يقبل لينك الريبو كامل أو `owner/repo`
- اختيار البرانش: `main` أو أي برانش تاني

### 💙 تبويب Google Drive - جديد في v1.1
- تسجيل دخول بحسابك الشخصي بـ OAuth
- رفع على أي فولدر Drive بالـ Folder ID بتاعه
- رفع المتعدل بس عن طريق مقارنة MD5
- تجديد التوكن تلقائياً بعد ما ينتهي
- مش محتاج تعمل Share للفولدر، لأنك بترفع بحسابك انت
- حل نهائي لمشكلة `Service Accounts do not have storage quota`

---

## 🆕 الجديد في v1.1

- Google Drive شغال بالكامل بـ OAuth
- أول تجربة: 86 ملف اترفعوا بنجاح على Drive
- تجديد توكن Drive تلقائي
- إصلاح مشكلة Build - Manifest package + Theme
- الاصدار بقى `1.1 - Trinity`
- 3 تبويبات مستقلة تماماً

---

## 🚀 طريقة الاستخدام - عربي

### Firebase
1. افتح تبويب Firebase
2. اختار ملف مفاتيح Firebase `.json`
3. اختار فولدر الموقع
4. دوس رفع على Firebase الان

### GH
1. افتح تبويب GH
2. حط التوكن `ghp_...`
3. حط الريبو `owner/repo` والبرانش `main`
4. دوس حفظ بيانات GH
5. اختار الفولدر ودوس رفع على GH

### Google Drive
1. افتح تبويب Drive
2. حط Client ID و Client Secret بتوع Desktop OAuth
3. دوس 1. هات كود تسجيل الدخول واختار ايميلك
4. انسخ الكود والصقه في البرنامج
5. دوس 2. تسجيل دخول بحسابك
6. حط Folder ID بتاع فولدر Drive
7. اختار الفولدر ودوس رفع على Drive

> ملحوظة مهمة لـ Drive:
> لازم تضيف ايميلك في OAuth consent screen > Audience > Test users
> وانت بتسجل اختار نفس الايميل اللي ضفته، غير كده هيطلعلك 403 Access Blocked.

---

# 🇺🇸 English Version

## 📥 Download

Download: **`DH-Trinity-v1.1.apk`**
from here:
https://github.com/doupl2018-create/firebase-hosting-deployer/releases/tag/v1.1-trinity

Install it on your phone, then enter your credentials inside the app.
**No token or key is stored in the source code.**

---

## ✨ Features

| Tab | What it does |
| :--- | :--- |
| 🔥 **Firebase** | Deploy a full website to Firebase Hosting |
| ⬛ **GH** | Upload a full folder to any GH repo |
| 💙 **Google Drive** | Upload a full folder to any Drive folder with your own account |

### 🔥 Firebase Tab
- Deploy a full website to Firebase Hosting
- Upload only changed files by comparing Hash
- GZIP compression before upload
- Login with Firebase Service Account `.json` file
- Each tab has its own folder and log

### ⬛ GH Tab
- Upload a full folder to any GH repo
- Upload only changed files, others are Skipped
- Clear counter at the end: Uploaded / Skipped / Failed
- Accepts full repo link or `owner/repo`
- Branch selection: `main` or any other branch

### 💙 Google Drive Tab - New in v1.1
- Login with your personal account via OAuth
- Upload to any Drive folder by its Folder ID
- Upload only changed files by comparing MD5
- Auto-refresh Drive token after expiry
- No need to Share the folder, because you upload with your own account
- Final fix for `Service Accounts do not have storage quota`

---

## 🆕 What's New in v1.1

- Google Drive fully working with OAuth
- First test: 86 files uploaded successfully to Drive
- Auto-refresh Drive token
- Build fix - Manifest package + Theme
- Version is now `1.1 - Trinity`
- 3 fully independent tabs

---

## 🚀 How to Use - English

### Firebase
1. Open Firebase tab
2. Select Firebase keys `.json` file
3. Select website folder
4. Tap Upload to Firebase Now

### GH
1. Open GH tab
2. Enter Token `ghp_...`
3. Enter Repo `owner/repo` and Branch `main`
4. Tap Save GH Data
5. Select folder and tap Upload to GH

### Google Drive
1. Open Drive tab
2. Enter Client ID and Client Secret of Desktop OAuth
3. Tap 1. Get Login Code and select your email
4. Copy the code and paste it in the app
5. Tap 2. Login with your account
6. Enter Folder ID of Drive folder
7. Select folder and tap Upload to Drive

> Important Note for Drive:
> You must add your email in OAuth consent screen > Audience > Test users
> When you login, select the same email you added, otherwise you will get 403 Access Blocked.

---

## 🛠️ Drive Setup From Scratch

1. Open Google Cloud Console
2. Enable Google Drive API
3. Create OAuth consent screen named `DH Deployer`
4. Add your email in Test users
5. Create OAuth client ID type Desktop app
6. Copy Client ID and Client Secret into the app

---

## 🔨 Build

- Open project in 【entity-Android Studio¦canonical_name=Android】
- applicationId: `com.dhautomation.firebasedeploy`
- versionCode: `2`
- versionName: `1.1 - Trinity`
- Do Clean + Rebuild
- APK will be in `app/build/outputs/apk/debug/`

---

## 🔒 Security

- All tokens and keys are entered inside the app after install
- No key or token is stored in the code
- Do not share your Client Secret with anyone, and do not upload it to GH

---

## DH Trinity

Made with ❤️ in Egypt
**DH Automation**
