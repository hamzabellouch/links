# 🔐 Signing Configuration

> [!CAUTION]
> The signing keystore (`*.jks`) is **never committed** to this repository.
> It must be kept private and stored securely outside version control.

## For Contributors: Building a Debug APK

No signing configuration is needed for debug builds. Simply:

```bash
git clone https://github.com/YOUR_USERNAME/links.git
cd links
./gradlew assembleDebug
```

## For Maintainers: Building a Signed Release APK

### 1. Generate a Keystore (first time only)

```bash
keytool -genkeypair -v \
  -keystore release_key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias links-key
```

Place the generated `release_key.jks` in the **project root** (it is git-ignored).

### 2. Create a `keystore.properties` file in the project root

```properties
storeFile=../release_key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=links-key
keyPassword=YOUR_KEY_PASSWORD
```

> [!IMPORTANT]
> `keystore.properties` is also git-ignored. Never commit it.

### 3. Build the signed release APK

```bash
./gradlew assembleRelease
```

The output APK will be in `app/release/`.
