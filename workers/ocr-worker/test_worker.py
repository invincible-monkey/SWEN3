import unittest
from unittest.mock import patch, MagicMock
import worker

class TestOcrWorker(unittest.TestCase):

    # --- Test for the successful path ---
    @patch('worker.get_document_metadata')
    @patch('worker.download_file_from_minio')
    @patch('worker.perform_ocr')
    @patch('worker.publish_result')
    def test_callback_success(self, mock_publish, mock_ocr, mock_download, mock_metadata):
        # Arrange: setup mocks and input data
        mock_channel = MagicMock()
        mock_method = MagicMock()
        mock_method.delivery_tag = 111 # unique tag for this test
        test_doc_id = 1
        test_storage_path = 'uuid-for-file-1'
        test_pdf_bytes = b'fake-pdf-content'
        test_extracted_text = 'Hello World'

        # mock return values
        mock_metadata.return_value = {'storagePath': test_storage_path}
        mock_download.return_value = test_pdf_bytes
        mock_ocr.return_value = test_extracted_text

        # Act: call the function under test
        worker.callback(mock_channel, mock_method, None, str(test_doc_id).encode('utf-8'))

        # Assert: verify external functions were called correctly
        mock_metadata.assert_called_once_with(test_doc_id)
        mock_download.assert_called_once_with(test_storage_path)
        mock_ocr.assert_called_once_with(test_pdf_bytes)
        mock_publish.assert_called_once_with(mock_channel, test_doc_id, test_extracted_text, 'SUCCESS')
        mock_channel.basic_ack.assert_called_once_with(delivery_tag=111)

    # --- Test for failure when fetching metadata ---
    @patch('worker.get_document_metadata')
    @patch('worker.publish_result')
    @patch('worker.download_file_from_minio')
    @patch('worker.perform_ocr')
    def test_callback_metadata_failure(self, mock_ocr, mock_download, mock_publish, mock_metadata):
        # Arrange
        mock_channel = MagicMock()
        mock_method = MagicMock()
        mock_method.delivery_tag = 222
        test_doc_id = 2
        mock_metadata.return_value = None # simulate metadata fetch failure

        # Act
        worker.callback(mock_channel, mock_method, None, str(test_doc_id).encode('utf-8'))

        # Assert
        mock_metadata.assert_called_once_with(test_doc_id)
        mock_download.assert_not_called() # ensure these were skipped
        mock_ocr.assert_not_called()
        # Check that a FAILED status was published with the correct error message
        mock_publish.assert_called_once_with(
            mock_channel, test_doc_id, "", "FAILED",
            str(ValueError(f"Could not fetch valid metadata or storagePath for doc ID {test_doc_id}."))
        )
        mock_channel.basic_ack.assert_called_once_with(delivery_tag=222) # Message should still be ack'd

    # --- Test for failure during MinIO download ---
    @patch('worker.get_document_metadata')
    @patch('worker.download_file_from_minio')
    @patch('worker.perform_ocr')
    @patch('worker.publish_result')
    def test_callback_download_failure(self, mock_publish, mock_ocr, mock_download, mock_metadata):
        # Arrange
        mock_channel = MagicMock()
        mock_method = MagicMock()
        mock_method.delivery_tag = 333
        test_doc_id = 3
        test_storage_path = 'uuid-for-file-3'
        mock_metadata.return_value = {'storagePath': test_storage_path}
        mock_download.return_value = None # simulate download failure

        # Act
        worker.callback(mock_channel, mock_method, None, str(test_doc_id).encode('utf-8'))

        # Assert
        mock_metadata.assert_called_once_with(test_doc_id)
        mock_download.assert_called_once_with(test_storage_path)
        mock_ocr.assert_not_called()
        mock_publish.assert_called_once_with(
            mock_channel, test_doc_id, "", "FAILED",
            str(ValueError(f"Could not download file {test_storage_path} from MinIO."))
        )
        mock_channel.basic_ack.assert_called_once_with(delivery_tag=333)

    # --- Test for failure during OCR processing ---
    @patch('worker.get_document_metadata')
    @patch('worker.download_file_from_minio')
    @patch('worker.perform_ocr')
    @patch('worker.publish_result')
    def test_callback_ocr_failure(self, mock_publish, mock_ocr, mock_download, mock_metadata):
        # Arrange
        mock_channel = MagicMock()
        mock_method = MagicMock()
        mock_method.delivery_tag = 444
        test_doc_id = 4
        test_storage_path = 'uuid-for-file-4'
        test_pdf_bytes = b'fake-pdf-content'
        ocr_error = Exception("Tesseract failed!")

        mock_metadata.return_value = {'storagePath': test_storage_path}
        mock_download.return_value = test_pdf_bytes
        mock_ocr.side_effect = ocr_error # simulate OCR raising an exception

        # Act
        worker.callback(mock_channel, mock_method, None, str(test_doc_id).encode('utf-8'))

        # Assert
        mock_metadata.assert_called_once_with(test_doc_id)
        mock_download.assert_called_once_with(test_storage_path)
        mock_ocr.assert_called_once_with(test_pdf_bytes)
        mock_publish.assert_called_once_with(
            mock_channel, test_doc_id, "", "FAILED", str(ocr_error)
        )
        mock_channel.basic_ack.assert_called_once_with(delivery_tag=444)

if __name__ == '__main__':
    unittest.main()