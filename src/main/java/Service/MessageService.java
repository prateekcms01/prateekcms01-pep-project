package Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DAO.MessageDao;
import DAO.DaoException;
import Model.Account;
import Model.Message;
import io.javalin.http.NotFoundResponse;

public class MessageService {
    private MessageDao messageDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
    private static final String DB_ACCESS_ERROR_MSG = "Error accessing the database";

    public MessageService() {
        messageDao = new MessageDao();
    }

    public MessageService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    // Get a Message by its ID using the MessageDao

    public Optional<Message> getMessageById(int id) {
        LOGGER.info("Fetching message with ID: {} ", id);
        try {
            Optional<Message> message = messageDao.getById(id);
            if (!message.isPresent()) {
                throw new ServiceException("Message not found");
            }
            LOGGER.info("Fetched message: {}", message.orElse(null));
            return message;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    // Get all messages using the MessageDao

    public List<Message> getAllMessages() {
        LOGGER.info("Fetching all messages");
        try {
            List<Message> messages = messageDao.getAll();
            LOGGER.info("Fetched {} messages", messages.size());
            return messages;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    // Get all messages posted by a specific account

    public List<Message> getMessagesByAccountId(int accountId) {
        LOGGER.info("Fetching messages posted by ID account: {}", accountId);
        try {
            List<Message> messages = messageDao.getMessagesByAccountId(accountId);
            LOGGER.info("Fetched {} messages", messages.size());
            return messages;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    // Insert a new message into the database using the MessageDao.

    public Message createMessage(Message message, Optional<Account> account) {
        LOGGER.info("Creating message: {}", message);

        if (!account.isPresent()) {
            throw new ServiceException("Account must exist when posting a new message");
        }

        validateMessage(message);

        checkAccountPermission(account.get(), message.getPosted_by());
        try {

            Message createdMessage = messageDao.insert(message);
            LOGGER.info("Created message: {}", createdMessage);
            return createdMessage;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    // Update an existing message in the database using the MessageDao.

    public Message updateMessage(Message message) {
        LOGGER.info("Updating message: {}", message.getMessage_id());

        Optional<Message> retrievedMessage = this.getMessageById(message.getMessage_id());

        if (!retrievedMessage.isPresent()) {
            throw new ServiceException("Message not found");
        }

        retrievedMessage.get().setMessage_text(message.getMessage_text());

        validateMessage(retrievedMessage.get());

        try {

            messageDao.update(retrievedMessage.get());
            LOGGER.info("Updated message: {}", message);
            return retrievedMessage.get();
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    // Delete an existing message from the database.

    public void deleteMessage(Message message) {
        LOGGER.info("Deleting message: {}", message);
        try {
            boolean hasDeletedMessage = messageDao.delete(message);
            if (hasDeletedMessage) {
                LOGGER.info("Deleted message {}", message);
            } else {
                throw new NotFoundResponse("Message to delete not found");
            }
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    // Validate a message by checking if the message_text is null, empty, or

    private void validateMessage(Message message) {
        LOGGER.info("Validating message: {}", message);
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            throw new ServiceException("Message text cannot be null or empty");
        }
        if (message.getMessage_text().length() > 254) {
            throw new ServiceException("Message text cannot exceed 254 characters");
        }
    }

    /*
     * Check if the account performing the action is the same as the one that posted
     * the message. This is used to maintain user data integrity and security.
     */

    private void checkAccountPermission(Account account, int postedBy) {
        LOGGER.info("Checking account permissions for messages");
        if (account.getAccount_id() != postedBy) {
            throw new ServiceException("Account not authorized to modify this message");
        }
    }
}