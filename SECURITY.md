# Security Policy

## Supported Versions

The following versions of Links are currently supported with security, privacy, and stability updates.

| Version       | Supported          |
| ------------- | ------------------ |
| + 1.0.0       | :white_check_mark: |
| < 1.0.0       | :x:                |



## Reporting a Vulnerability

If you discover a security vulnerability, privacy issue, unexpected behavior with permissions, or any issue that could negatively affect users or their data safety, please report it responsibly.

### Before Reporting
Please make sure that:
- The issue is reproducible
- You are using the latest supported version of Links
- The issue is not caused by third-party modifications, custom ROMs, or unsupported Android environments

### How to Report
You can report vulnerabilities through:
- GitHub Issues (for non-sensitive reports and general bug reports)
- Direct private contact channels / email for sensitive vulnerabilities or security concerns

When reporting, please include:
- Device model and Android OS version
- Links application version
- Granted permissions state (e.g., Camera, Notifications)
- Steps to reproduce the issue
- Screenshots or system logcat outputs if available
- A clear explanation of the potential security or privacy impact

### Response Policy
Security reports are reviewed as quickly as possible.  
If the issue is confirmed:
- The vulnerability will be investigated, verified, and patched
- A fix will be included in the next update release
- Credit may be given to the reporter if requested

If the report is invalid, incomplete, or not reproducible, it may be closed without action.



## Security & Architecture Notes

Links is designed with a strict **Privacy-First & On-Device Security Model**:

- **Local Execution:** QR code scanning and generation operate 100% locally on your device. No camera frames, scanned content, or generated QR data is transmitted to external servers.
- **Link Inspection Safety:** HTTP redirect resolution is performed using lightweight, read-only HEAD/GET requests strictly to inspect the final destination URL of a link. No user credentials, cookies, or personal browsing data are sent or stored.
- **Root-Free Operation:** Links does not require root privileges. It relies entirely on official Android system APIs (`CameraX`, `HttpURLConnection`, `OkHttp`).
- **Camera Isolation:** The camera hardware is accessed exclusively when the user is actively on the QR Scanner screen. No background recording, silent frame capture, or image storage takes place at any time.
- **Transient Memory Handling:** All camera frames and temporary network responses held during processing are automatically cleared from memory upon request completion, application pause, or termination to keep the footprint minimal and secure.
