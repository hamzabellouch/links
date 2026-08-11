# Privacy Policy - Links

Last Updated: August 11, 2026


### 1. Executive Summary & Overview:

Links is an Android application designed to assist users with link security inspection, short URL redirect resolution, QR code scanning, and custom QR code generation.
#### Core Privacy Commitment:
Links operates "100% on-device" for QR scanning, matrix generation, and local utilities. We do not collect, store, transmit, share, or sell any personal data, usage logs, device identifiers, camera captures, or browsed URLs to external analytics servers or third parties.


### 2. Information Processed On-Device:
To perform its core URL management and QR code features, Links reads and processes the following information `locally on your device`:

- **Camera Preview Feed:** Processed in real-time within volatile memory (RAM) strictly to detect and decode QR code matrices. Camera frames are never saved, recorded, stored to disk, or transmitted.
- **Scanned & Generated Contents:** Text strings and web URLs entered or scanned by the user are processed locally to render QR code graphics or display detailed destination results.
- **Local History & Preferences:** Scanned/generated history and minimal app options (such as theme preferences and language settings) are stored locally on the device using Android "SharedPreferences".


### 3. Permissions Used & Their Purposes:
Links requests specific Android permissions strictly to deliver its core functionality:

**A. Camera Permission** (`android.permission.CAMERA`)
- **Purpose:** Enables the app to open the device camera for real-time QR code scanning via Android CameraX.
- **Scope:** Used exclusively within the interactive scanner view. Camera stream data is processed locally and discarded immediately.

**B. Internet & Network State** (`INTERNET` & `ACCESS_NETWORK_STATE`)
- **Purpose:** Allows Links to resolve short URLs (following HTTP redirect chains to display final target destinations for security inspection) and check for app updates on GitHub.
- **Scope:** Used strictly for network requests initiated by link resolving or auto-update checks. No telemetry or user profile data is sent.

**C. Request Install Packages** (`REQUEST_INSTALL_PACKAGES`)
- **Purpose:** Enables Links to prompt the user to install updated APK packages downloaded via the built-in update checker.
- **Scope:** Used strictly when the user chooses to update the app.

**D. Post Notifications** (`POST_NOTIFICATIONS`)
- **Purpose:** Displays download progress and status notifications when fetching app updates on Android 13+.
- **Scope:** Used strictly for system notification alerts.


### 4. Camera & Link Resolving Safety Policy:
Links complies fully with privacy guidelines regarding camera usage and network safety:

- **Strict Camera Isolation:** The camera hardware is accessed solely when the user is on the QR Scanner screen. No background recording or silent frame capture takes place.
- **Zero Personal Data Extraction:** Link redirect checks inspect HTTP header locations to resolve destination URLs. Links never reads, captures, or transmits personal browsing history, credentials, or cookies.
- **Full User Consent & Control:** Camera access requires explicit runtime permission prompts and can be revoked at any time via Android Settings.


### 5. Data Sharing, Analytics & Advertising:

- **No Data Sharing:** No personal user data or camera feeds leave your device.
- **No Third-Party Analytics / Tracking:** Links does not integrate third-party telemetry, tracking SDKs, or analytics services.
- **No Ads:** Links does not display targeted or behavioral advertisements.


### 6. Data Retention & Lifecycle:

- **Volatile Frame Wiping:** All camera frames and transient network responses are cleared from RAM immediately after processing.
- **Complete Data Removal:** Uninstalling Links immediately and permanently deletes all local history and app preferences stored on the device.


### 7. Managing Permissions & User Rights:
You maintain full control over the permissions granted to Links. You may revoke any permission at any time via Android Settings:

**A. Disable Camera Permission:** `Settings > Apps > Links > Permissions > Camera > Don't Allow`

**B. Disable Notifications:** `Settings > Apps > Links > Notifications > Turn Off`


### 8. Contact & Support:
If you have any questions or feedback regarding this Privacy Policy or permission usage, please contact us at:
Email: hamzabellouchcontact@gmail.com


-------------------------------------------



# سياسة الخصوصية - Links
آخر تحديث: ١١ أغسطس ٢٠٢٦

### ١. الملخص التنفيذي والنظرة العامة:

تطبيق Links هو تطبيق أدوات مساعد لنظام Android، صُمم لمساعدة المستخدمين في فحص أمان الروابط، وفك إعادة توجيه الروابط المختصرة، وقراءة وتوليد رموز QR المخصصة.
الالتزام الأساسي بالخصوصية: يعمل Links بنسبة "١٠٠٪ على الجهاز" بالنسبة لقراءة وتوليد رموز QR والأدوات المحلية. نحن لا نجمع أو نخزن أو ننقل أو نشارك أو نبيع أي بيانات شخصية أو سجلات استخدام أو معرّفات الجهاز أو لقطات الكاميرا أو الروابط المصفحة إلى خوادم تحليلات خارجية أو أطراف ثالثة.

٢. المعلومات التي تتم معالجتها على الجهاز:
لتنفيذ ميزاته الأساسية الخاصة بإدارة الروابط ورموز QR، يقوم Links بقراءة ومعالجة المعلومات التالية "محليًا على جهازك":

* بث المعاينة من الكاميرا: يتم معالجته في الوقت الفعلي داخل الذاكرة المؤقتة (RAM) فقط لاكتشاف وفك شفرة مصفوفات رموز QR. لا يتم حفظ إطارات الكاميرا أو تسجيلها أو تخزينها على قرص التخزين أو نقلها مطلقًا.
* المحتويات الممسوحة والمولدة: يتم معالجة النصوص والروابط التي يدخلها أو يمسحها المستخدم محليًا لإنشاء رسوم بيانية لرمز QR أو عرض تفاصيل الوجهة النهائية.
* السجل المحلي والتفضيلات: يتم تخزين سجل الروابط والرموز الممسوحة/المولدة والتفضيلات البسيطة (مثل إعدادات المظهر واللغة) محليًا على الجهاز باستخدام Android "SharedPreferences".

٣. الأذونات المستخدمة وأغراضها:
يطلب Links أذونات Android محددة فقط لتقديم وظائفه الأساسية:

أ. إذن الكاميرا (android.permission.CAMERA)

* الغرض: يسمح لـ Links بفتح كاميرا الجهاز لقراءة رموز QR في الوقت الفعلي عبر مكتبة Android CameraX.
* النطاق: يُستخدم حصريًا داخل شاشة الماسح الضوئي التفاعلية. يتم معالجة بيانات الكاميرا محليًا والتخلص منها فورًا.

ب. إذن الإنترنت وحالة الشبكة (INTERNET & ACCESS_NETWORK_STATE)

* الغرض: يسمح لـ Links بتتبع إعادة توجيه الروابط المختصرة (متابعة سلسلة تحويلات HTTP لعرض الوجهة النهائية لفحص أمانها) والتحقق من تحديثات التطبيق على GitHub.
* النطاق: يُستخدم بشكل صارم لطلبات الشبكة الناتجة عن فحص الروابط أو التحقق من التحديثات. لا يتم إرسال أي بيانات تتبع أو ملفات تعريف.

ج. إذن طلب تثبيت الحزم (REQUEST_INSTALL_PACKAGES)

* الغرض: يمكّن Links من مطالبة المستخدم بتثبيت تحديثات التطبيق التي تم تنزيلها عبر أداة التحديث المدمجة.
* النطاق: يُستخدم بشكل صارم عندما يختار المستخدم تحديث التطبيق.

د. إذن إرسال الإشعارات (POST_NOTIFICATIONS)

* الغرض: يعرض تقدم التنزيل وتنبيهات حالة تحديث التطبيق على نظام Android 13 فما فوق.
* النطاق: يُستخدم بشكل صارم لإشعارات التحديث بالنظام.

٤. سياسة أمان الكاميرا وفحص الروابط:
يلتزم Links التزامًا كاملًا بسياسات الخصوصية المتعلقة باستخدام الكاميرا وأمان الشبكة:

* عزلة صارمة للكاميرا: يتم الوصول إلى الكاميرا فقط عندما يكون المستخدم في شاشة ماسح QR. لا يتم أي تسجيل في الخلفية أو التقاط صامت للإطارات.
* عدم استخراج البيانات الشخصية: يقوم فحص الروابط بمراجعة عناوين HTTP للتحقق من الروابط النهائية، ولا يقرأ Links أو يلتقط أو ينقل سجل التصفح الشخصي أو بيانات الاعتماد أو ملفات الكوكيز.
* موافقة المستخدم والتحكم: يتطلب الوصول إلى الكاميرا إذنًا صريحًا من المستخدم، ويمكن للمستخدم إلغاؤه في أي وقت من إعدادات Android.

٥. مشاركة البيانات والتحليلات والإعلانات:

* عدم مشاركة البيانات: لا تغادر أي بيانات شخصية للمستخدم أو بث من الكاميرا جهازه.
* عدم وجود تحليلات أو تتبع من جهات خارجية: لا يدمج Links أي خدمات Telemetry أو SDKs للتتبع أو التحليلات من جهات خارجية.
* لا توجد إعلانات: لا يعرض Links إعلانات موجهة أو قائمة على السلوك.

٦. الاحتفاظ بالبيانات ودورة حياتها:

* مسح الإطارات والبيانات المؤقتة: يتم مسح جميع إطارات الكاميرا واستجابات الشبكة المؤقتة من الذاكرة (RAM) فورًا بعد معالجتها.
* الإزالة الكاملة للبيانات: يؤدي إلغاء تثبيت Links إلى حذف جميع السجلات والتفضيلات المحلية المخزنة على الجهاز فورًا وبشكل دائم.

٧. إدارة الأذونات وحقوق المستخدم:
تحتفظ بالتحكم الكامل في الأذونات الممنوحة لـ Links. يمكنك إلغاء أي إذن في أي وقت عبر إعدادات Android:

أ. تعطيل إذن الكاميرا: الإعدادات > التطبيقات > Links > الأذونات > الكاميرا > عدم السماح

ب. تعطيل الإشعارات: الإعدادات > التطبيقات > Links > الإشعارات > إيقاف التشغيل

٨. التواصل والدعم:
إذا كانت لديك أي أسئلة أو ملاحظات بشأن سياسة الخصوصية هذه أو استخدام الأذونات، فيرجى التواصل معنا على:
البريد الإلكتروني: hamzabellouchcontact@gmail.com
