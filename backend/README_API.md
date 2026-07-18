# MCCMS: Future API Integration Guide

This document outlines the steps required to transition the TikTok and Instagram "Mocks" into live production integrations.

## 📱 TikTok Content Posting API
To enable real TikTok uploads:
1.  **Register App**: Create an account on the [TikTok for Developers](https://developers.tiktok.com/) portal.
2.  **Scopes**: Request the `video.upload` scope.
3.  **App Review**: TikTok requires a manual audit of your app. You must provide a screen recording of your app's workflow.
4.  **Implementation**: Swap `TikTokPublisher.java` logic to use TikTok's `/video/upload/` endpoint.
5.  **OAuth**: Implement the TikTok Login kit to obtain user access tokens.

## 📸 Instagram Reels API (Meta Graph API)
To enable real Instagram publishing:
1.  **Meta App**: Create a "Business" type app on the [Meta App Dashboard](https://developers.facebook.com/).
2.  **Requirements**:
    - A Facebook Page linked to an Instagram Business or Creator account.
    - `instagram_content_publish` permission.
3.  **App Review**: Meta requires a detailed "App Review" process before you can publish to public accounts.
4.  **Implementation**: Use the Meta Graph API `/media` and `/media_publish` endpoints.
5.  **Workflow**: Upload the video to a public URL (S3), provide that URL to Meta, and then confirm the publication once Meta has processed the video.

## 🔑 Common OAuth Strategy
The system is built to store `CONNECTED_ACCOUNTS` (see schema). To go live:
- Create a new table `CONNECTED_ACCOUNTS` to store `access_token` and `refresh_token` per user/platform.
- In `PublishingService`, retrieve the token for the specific user before calling `publisher.publish(deliverable, token)`.
