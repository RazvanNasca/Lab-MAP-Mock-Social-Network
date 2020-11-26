package socialnetwork.service;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Message;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.MessageValidator;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

import java.util.ArrayList;
import java.util.Random;

public class MessageService
{
    private final Repository<Long, Message> messageRepository;
    private final Repository<Long, User> userRepository;
    private final Repository<Tuple<Long,Long>, Friendship> friendshipRepository;
    private final Validator<Message> messageValidator;
    private Long messageID = 0L;

    public MessageService(Repository<Long, Message> messageRepository, Repository<Long, User> userRepository, Repository<Tuple<Long, Long>, Friendship> friendshipRepository, MessageValidator messageValidator)
    {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.messageValidator = messageValidator;
    }

    public Long generateMessageID()
    {
        return messageID++;
    }

    public void sendMessage(Message message)
    {
        this.messageValidator.validate(message);

        ArrayList<Long> recipients = message.getTo();
        ArrayList<Long> finalRecipients = new ArrayList<>();

        final String[] errorMessage = {""};
        recipients.forEach(
                x-> { if(this.friendshipRepository.findOne(new Tuple<>(x, message.getFrom())) != null)
                {
                    finalRecipients.add(x);
                }
                else
                {
                    errorMessage[0] += "You are not friends with user " + x + "!\n";
                }
                });

        message.setTo(finalRecipients);

        if(finalRecipients.size() > 0)
            this.messageRepository.save(message);

        if(errorMessage[0].compareTo("") != 0)
            throw new ServiceException(errorMessage[0]);
    }

    public Message reply(Message message)
    {
        Random random = new Random();
        while(this.messageRepository.findOne(message.getId()) != null)
        {
            message.setId(random.nextLong());
        }
        return this.messageRepository.save(message);
    }

    public Message getMessage(long id)
    {
        return this.messageRepository.findOne(id);
    }
}
