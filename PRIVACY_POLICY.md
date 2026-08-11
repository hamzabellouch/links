# Privacy Policy - Apex

Last Updated: July 30, 2026


### 1. Executive Summary & Overview:

Apex is an Android utility application designed to assist users with cache cleaning, storage optimization, and system memory (RAM) management. 
#### Core Privacy Commitment:
Apex operates "100% on-device". We do not collect, store, transmit, share, or sell any personal data, usage logs, device identifiers, or lists of installed applications to external servers or third parties.


### 2. Information Processed On-Device:
To perform its core device optimization features, Apex reads and processes the following information `locally on your device`:

- **Installed Application List:** Scanned locally to display installed packages, their cache sizes, and memory usage within the app interface.
- **Storage & RAM Metrics:** Calculated dynamically on-device to present accurate storage and memory usage statistics.
- **Local Preferences:** Minimal app states (such as onboarding completion) stored locally using Android "SharedPreferences".


### 3. Permissions Used & Their Purposes:
Apex requests specific Android permissions strictly to deliver its core functionality:

**A. Usage Access** (`PACKAGE_USAGE_STATS`)
- **Purpose:** Allows Apex to read read-only storage statistics and cache sizes per application.
- **Scope:** Used exclusively locally for storage calculation.

**B. Query All Packages** (`QUERY_ALL_PACKAGES`)

- **Purpose:** Enables Apex to enumerate installed applications so users can review and clean app caches.
- **Scope:** Used strictly within local UI rendering.

**C. Accessibility Service** (`BIND_ACCESSIBILITY_SERVICE`)
- **Purpose:** Automates navigation through system settings to perform batch "Clear Cache" and "Force Stop" actions requested by the user.


### 4. Accessibility Service Prominent Disclosure & Policy:
Apex complies fully with Google Play policies regarding Accessibility Services:

- **Strict Functional Scope:** The Accessibility Service is utilized "solely" to perform automated UI button clicks (e.g., navigating to : Settings > Apps > Storage > Clear Cache) on behalf of the user.
- **Zero Data Extraction:** The Accessibility Service "never" reads, captures, logs, or transmits personal text, passwords, messages, keystrokes, or financial data displayed on the screen.
- **User Consent & Control:** The Accessibility Service requires explicit user enablement in Android Settings and can be revoked by the user at any time.


### 5. Data Sharing, Analytics & Advertising:

- **No Data Sharing:** No user data leaves your device.
- **No Third-Party Analytics / Tracking:** Apex does not integrate third-party telemetry, tracking SDKs, or analytics services.
- **No Ads:** Apex does not display targeted or behavioral advertisements.


### 6. Data Retention & Lifecycle:

- **Memory Cache Wiping:** All transient in-memory caches held by Apex are automatically cleared whenever the app is closed, paused, or trimmed by the system.
- **Complete Data Removal:** Uninstalling Apex immediately and permanently deletes all local preferences stored on the device.


### 7. Managing Permissions & User Rights:
You maintain full control over the permissions granted to Apex. You may revoke any permission at any time via Android Settings:

**A. Disable Accessibility Service:** `Settings > Accessibility > Installed Apps > Apex > Turn Off`

**B. Revoke Usage Access:** `Settings > Apps > Special App Access > Usage Access > Apex > Turn Off`


### 8. Contact & Support:
If you have any questions or feedback regarding this Privacy Policy or permission usage, please contact us at:
Email: hamzabellouchcontact@gmail.com


-------------------------------------------



# سياسة الخصوصية - Apex
آخر تحديث: ٣٠ يوليو ٢٠٢٦

### ١. الملخص التنفيذي والنظرة العامة:

تطبيق Apex هو تطبيق أدوات مساعد لنظام Android، صُمم لمساعدة المستخدمين في تنظيف ذاكرة التخزين المؤقت (Cache)، وتحسين مساحة التخزين، وإدارة ذاكرة النظام (RAM).
الالتزام الأساسي بالخصوصية: يعمل Apex بنسبة "١٠٠٪ على الجهاز". نحن لا نجمع أو نخزن أو ننقل أو نشارك أو نبيع أي بيانات شخصية أو سجلات استخدام أو معرّفات الجهاز أو قوائم التطبيقات المثبتة إلى خوادم خارجية أو أطراف ثالثة.

٢. المعلومات التي تتم معالجتها على الجهاز:
لتنفيذ ميزاته الأساسية الخاصة بتحسين أداء الجهاز، يقوم Apex بقراءة ومعالجة المعلومات التالية "محليًا على جهازك":

* قائمة التطبيقات المثبتة: يتم فحصها محليًا لعرض الحزم المثبتة، وأحجام ذاكرة التخزين المؤقت الخاصة بها، واستخدام الذاكرة داخل واجهة التطبيق.
* مقاييس التخزين وRAM: يتم احتسابها ديناميكيًا على الجهاز لعرض إحصاءات دقيقة حول استخدام مساحة التخزين والذاكرة.
* التفضيلات المحلية: يتم تخزين الحد الأدنى من حالات التطبيق (مثل إكمال شاشة الترحيب) محليًا باستخدام Android "SharedPreferences".

٣. الأذونات المستخدمة وأغراضها:
يطلب Apex أذونات Android محددة فقط لتقديم وظائفه الأساسية:

أ. إذن الوصول إلى الاستخدام (PACKAGE_USAGE_STATS)

* الغرض: يسمح لـ Apex بقراءة إحصاءات التخزين للقراءة فقط وأحجام ذاكرة التخزين المؤقت لكل تطبيق.
* النطاق: يُستخدم حصريًا محليًا لحساب مساحة التخزين.

ب. الاستعلام عن جميع الحزم (QUERY_ALL_PACKAGES)

* الغرض: يمكّن Apex من تعداد التطبيقات المثبتة حتى يتمكن المستخدمون من مراجعة وتنظيف ذاكرة التخزين المؤقت الخاصة بالتطبيقات.
* النطاق: يُستخدم بشكل صارم داخل عرض واجهة المستخدم المحلية.

ج. خدمة إمكانية الوصول (BIND_ACCESSIBILITY_SERVICE)

* الغرض: تؤتمت التنقل عبر إعدادات النظام لتنفيذ إجراءات "مسح ذاكرة التخزين المؤقت" و"الإيقاف الإجباري" بشكل جماعي بناءً على طلب المستخدم.

٤. الإفصاح البارز عن خدمة إمكانية الوصول والسياسة:
يلتزم Apex التزامًا كاملًا بسياسات Google Play المتعلقة بخدمات إمكانية الوصول:

* نطاق وظيفي صارم: تُستخدم خدمة إمكانية الوصول "فقط" لتنفيذ نقرات تلقائية على أزرار واجهة المستخدم (على سبيل المثال: الإعدادات > التطبيقات > التخزين > مسح ذاكرة التخزين المؤقت) نيابةً عن المستخدم.
* عدم استخراج البيانات: خدمة إمكانية الوصول "لا" تقرأ أو تلتقط أو تسجل أو تنقل أي نصوص شخصية أو كلمات مرور أو رسائل أو ضغطات مفاتيح أو بيانات مالية معروضة على الشاشة.
* موافقة المستخدم والتحكم: تتطلب خدمة إمكانية الوصول تمكينًا صريحًا من المستخدم في إعدادات Android، ويمكن للمستخدم إلغاء تفعيلها في أي وقت.

٥. مشاركة البيانات والتحليلات والإعلانات:

* عدم مشاركة البيانات: لا تغادر أي بيانات للمستخدم جهازه.
* عدم وجود تحليلات أو تتبع من جهات خارجية: لا يدمج Apex أي خدمات Telemetry أو SDKs للتتبع أو التحليلات من جهات خارجية.
* لا توجد إعلانات: لا يعرض Apex إعلانات موجهة أو قائمة على السلوك.

٦. الاحتفاظ بالبيانات ودورة حياتها:

* مسح ذاكرة التخزين المؤقت في الذاكرة: يتم مسح جميع بيانات ذاكرة التخزين المؤقت المؤقتة الموجودة في الذاكرة والتي يحتفظ بها Apex تلقائيًا كلما تم إغلاق التطبيق أو إيقافه مؤقتًا أو تقليصه بواسطة النظام.
* الإزالة الكاملة للبيانات: يؤدي إلغاء تثبيت Apex إلى حذف جميع التفضيلات المحلية المخزنة على الجهاز فورًا وبشكل دائم.

٧. إدارة الأذونات وحقوق المستخدم:
تحتفظ بالتحكم الكامل في الأذونات الممنوحة لـ Apex. يمكنك إلغاء أي إذن في أي وقت عبر إعدادات Android:

أ. تعطيل خدمة إمكانية الوصول: الإعدادات > إمكانية الوصول > التطبيقات المثبتة > Apex > إيقاف التشغيل

ب. إلغاء إذن الوصول إلى الاستخدام: الإعدادات > التطبيقات > الوصول الخاص للتطبيقات > الوصول إلى الاستخدام > Apex > إيقاف التشغيل

٨. التواصل والدعم:
إذا كانت لديك أي أسئلة أو ملاحظات بشأن سياسة الخصوصية هذه أو استخدام الأذونات، فيرجى التواصل معنا على:
البريد الإلكتروني: hamzabellouchcontact@gmail.com
