# DH Firebase Deployer - 3 في 1 | 3 in 1

برنامج أندرويد بيرفع موقعك على 3 خدمات من برنامج واحد، كل خدمة مستقلة تماماً ببياناتها وفولدرها وسجلها.
An Android app to deploy your website to 3 services from one app, each service is fully independent with its own data, folder and log.

---

## 🇪🇬 بالعربي

### المميزات
- **Firebase Hosting:** بيرفع الملفات المتعدلة بس، وبيتخطى اللي متغيرش
- **【entity-GitHub¦canonical_name=GitHub】:** بيرفع الملفات المتعدلة بس، وبيقولك في الآخر اترفع كام واتخطى كام
- **Google Drive:** بيرفع الملفات المتعدلة بس، وبيتخطى اللي موجود بنفس المحتوى
- **3 تبويبات مستقلة:** كل تبويب ليه بياناته وفولدره وسجله، مفيش حاجة بتتلخبط على التانية
- **الأمان:** التوكن والمفاتيح مش موجودة في الكود، انت بتحطها جوه البرنامج بعد التثبيت

### طريقة التشغيل

#### 1. Firebase
1. افتح تبويب Firebase
2. اختار ملف مفاتيح Firebase (.json)
3. اختار فولدر الموقع من موبايلك
4. دوس "رفع على Firebase الآن"

#### 2. GitHub
1. افتح تبويب GH
2. حط التوكن (ghp_...) والريبو (owner/repo) والبرانش (main)
3. دوس "حفظ بيانات GH"
4. اختار فولدر GitHub
5. دوس "رفع على GH"

#### 3. Google Drive
1. افتح تبويب Drive
2. سجل دخول بحساب جوجل بتاعك
3. حط ID فولدر Drive (اللي بعد folders/ في اللينك)
4. دوس "حفظ بيانات Drive"
5. اختار الفولدر من موبايلك
6. دوس "رفع على Drive"

### ملحوظة مهمة عن Drive
لازم تعمل Share لفولدر Drive لحسابك، وتديله صلاحية Editor، غير كده الرفع هيرفض.

### البناء من المصدر
1. افتح المشروع في Android Studio
2. اعمل Sync و Build
3. ثبت الـ APK على موبايلك

---

## 🇺🇸 English

### Features
- **Firebase Hosting:** Uploads only changed files, skips unchanged files
- **GitHub:** Uploads only changed files, shows final count: uploaded / skipped
- **Google Drive:** Uploads only changed files, skips files with same content
- **3 Independent Tabs:** Each tab has its own credentials, folder and log, no cross-contamination
- **Security:** No tokens or keys in code, you enter them inside the app after install

### How to Use

#### 1. Firebase
1. Open Firebase tab
2. Select Firebase service-account (.json) file
3. Select your website folder from your phone
4. Tap "Upload to Firebase"

#### 2. 【entity-GitHub¦canonical_name=GitHub】
1. Open GH tab
2. Enter Token (ghp_...), Repo (owner/repo) and Branch (main)
3. Tap "Save GH Data"
4. Select GitHub folder
5. Tap "Upload to GH"

#### 3. Google Drive
1. Open Drive tab
2. Sign in with your Google account
3. Enter Drive Folder ID (after folders/ in the link)
4. Tap "Save Drive Data"
5. Select folder from your phone
6. Tap "Upload to Drive"

### Important Note for Drive
You must Share your Drive folder with your account and give it Editor permission, otherwise upload will fail.

### Build from Source
1. Open project in Android Studio
2. Sync and Build
3. Install APK on your phone

---

## 👨‍💻 Developer
Made by **DH Automation**
- Package: `com.dhautomation.firebasedeploy`

## 📝 License
Open Source - Free to use and modify
