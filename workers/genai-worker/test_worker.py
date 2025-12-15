import unittest
from unittest.mock import patch, MagicMock
import worker

class TestGenAiWorker(unittest.TestCase):

    @patch('worker.get_document_content')
    @patch('worker.generate_summary')
    @patch('worker.publish_result')
    def test_callback_success(self, mock_publish, mock_generate, mock_metadata):
        # Arrange
        mock_channel = MagicMock()
        mock_method = MagicMock()
        mock_method.delivery_tag = 123
        
        doc_id = 1
        fake_content = "This is a long invoice text."
        fake_summary = "Invoice summary."
        
        mock_metadata.return_value = {'content': fake_content}
        mock_generate.return_value = fake_summary

        # Act
        worker.callback(mock_channel, mock_method, None, str(doc_id).encode('utf-8'))

        # Assert
        mock_metadata.assert_called_once_with(doc_id)
        mock_generate.assert_called_once_with(fake_content)
        mock_publish.assert_called_once_with(
            mock_channel, doc_id, fake_summary, "COMPLETED"
        )
        mock_channel.basic_ack.assert_called_once_with(delivery_tag=123)

    @patch('worker.get_document_content')
    @patch('worker.publish_result')
    def test_callback_api_failure(self, mock_publish, mock_metadata):
        # Arrange
        mock_channel = MagicMock()
        mock_method = MagicMock()
        mock_method.delivery_tag = 456
        doc_id = 2
        
        mock_metadata.return_value = None

        # Act
        worker.callback(mock_channel, mock_method, None, str(doc_id).encode('utf-8'))

        # Assert
        mock_publish.assert_called_once_with(
            mock_channel, doc_id, "", "FAILED", 
            "Could not fetch valid content for doc ID 2."
        )
        mock_channel.basic_ack.assert_called_once_with(delivery_tag=456)

if __name__ == '__main__':
    unittest.main()