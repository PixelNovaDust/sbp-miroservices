package com.sbp.common.storage;

import java.net.URL;

public interface ObjectStorageClient {

	UploadResult upload(UploadRequest request);

	PresignedUpload presignUpload(PresignUploadRequest request);

	URL presignDownload(DownloadRequest request);

	ObjectHead head(String bucket, String objectKey);

	void delete(String bucket, String objectKey);

}
