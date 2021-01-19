package socialnetwork.domain.validators;

import socialnetwork.domain.Event;

import java.time.LocalDateTime;

public class EventValidator implements Validator<Event>
{
    @Override
    public void validate(Event entity) throws ValidationException {

        String errorMessage = "";

        if(entity.getName().equals(""))
            errorMessage += "Event must have a name!\n";

        if(entity.getDescriere().equals(""))
            errorMessage += "Event must have a description!\n";

        if(entity.getData().isBefore(LocalDateTime.now()))
            errorMessage += "Event must be in the future!\n";

        if(!errorMessage.equals(""))
            throw new ValidationException(errorMessage);

    }
}
