package socialnetwork.repository.database;

import socialnetwork.domain.Event;
import socialnetwork.domain.Message;
import socialnetwork.domain.Tuple;
import socialnetwork.paging.*;
import socialnetwork.repository.Repository;
import socialnetwork.repository.RepositoryException;
import java.time.LocalDateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventDatabaseRepository implements PagingRepository<Long, Event>
{
    private Connection connection;

    public EventDatabaseRepository(String url, String username, String password)
    {
        try
        {
            this.connection = DriverManager.getConnection(url, username, password);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    
    @Override
    public Event findOne(Long id)
    {
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM eveniment WHERE id = (?)");
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next())
                return null;

            long idEvent = resultSet.getLong("id");
            String nume = resultSet.getString("name");
            String descriere = resultSet.getString("descriere");
            LocalDateTime date = resultSet.getTimestamp("data").toLocalDateTime();

            PreparedStatement statement2 = this.connection.prepareStatement("SELECT * FROM utilizatoreveniment WHERE ideveniment = (?)");
            statement2.setLong(1, idEvent);
            resultSet = statement2.executeQuery();

            ArrayList<Long> participanti = new ArrayList<>();
            while(resultSet.next())
            {
                participanti.add(resultSet.getLong("idutilizator"));
            }

            Event event = new Event(nume, descriere, date, participanti);
            event.setId(idEvent);
            return event;

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Event> findAll()
    {
        Set<Event> events = new HashSet<>();
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM eveniment");
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next())
            {
                long idEvent = resultSet.getLong("id");
                String nume = resultSet.getString("name");
                String descriere = resultSet.getString("descriere");
                LocalDateTime date = resultSet.getTimestamp("data").toLocalDateTime();

                PreparedStatement statement2 = this.connection.prepareStatement("SELECT * FROM utilizatoreveniment WHERE ideveniment = (?)");
                statement2.setLong(1, idEvent);
                ResultSet resultSet2 = statement2.executeQuery();

                ArrayList<Long> participanti = new ArrayList<>();
                while(resultSet2.next())
                {
                    participanti.add(resultSet2.getLong("idutilizator"));
                }

                Event event = new Event(nume, descriere, date, participanti);
                event.setId(idEvent);

                events.add(event);
            }
        }catch (SQLException e)
        {
            e.printStackTrace();
        }

        return events;
    }

    public Tuple<Long, Long> findOneSub(Tuple<Long, Long> tuple)
    {
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM utilizatoreveniment WHERE idutilizator = (?) AND ideveniment = (?)");
            statement.setLong(1, tuple.getLeft());
            statement.setLong(2, tuple.getRight());
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next())
                return null;

            long idUser = resultSet.getLong("idutilizator");
            long idEvent = resultSet.getLong("ideveniment");

            return new Tuple<>(idUser, idEvent);

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Tuple<Long, Long> addSub(Tuple<Long, Long> tuple)
    {
        if(this.findOneSub(tuple) != null)
        {
            throw new RepositoryException("User already subs!");
        }

        try
        {
            String not = "ON";
            PreparedStatement statement = this.connection.prepareStatement("INSERT INTO utilizatoreveniment(idutilizator, ideveniment, notificari) VALUES (?, ?, ?)");

            statement.setLong(1, tuple.getLeft());
            statement.setLong(2, tuple.getRight());
            statement.setString(3, not);

            statement.executeUpdate();


        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return tuple;
    }

    public void deleteSub(Tuple<Long, Long> tuple)
    {
        if(this.findOneSub(tuple) == null)
        {
            throw new RepositoryException("User is not subs at this event!");
        }

        try
        {
            PreparedStatement statement = this.connection.prepareStatement("DELETE FROM utilizatoreveniment WHERE idutilizator = (?) AND ideveniment = (?)");

            statement.setLong(1, tuple.getLeft());
            statement.setLong(2, tuple.getRight());

            statement.executeUpdate();


        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void updateEvent(Tuple<Long, Long> id, String not)
    {
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("UPDATE utilizatoreveniment SET notificari = (?) WHERE idutilizator = (?) AND ideveniment = (?)");

            statement.setString(1, not);
            statement.setLong(2, id.getLeft());
            statement.setLong(3, id.getRight());
            statement.executeUpdate();

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public String findNotification(Tuple<Long, Long> id)
    {
        String rez = "";
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT notificari FROM utilizatoreveniment WHERE idutilizator = (?) AND ideveniment = (?)");

            statement.setLong(1, id.getLeft());
            statement.setLong(2, id.getRight());
            //statement.executeUpdate();
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next())
                return null;

            String not = resultSet.getString("notificari");
            rez = not;

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return rez;
    }

    @Override
    public Event save(Event entity) 
    {
        if(this.findOne(entity.getId()) != null)
        {
            throw new RepositoryException("ID already exists!");
        }

        try
        {
            PreparedStatement statement1 = this.connection.prepareStatement("INSERT INTO Eveniment(id, name, descriere, data) VALUES (?, ?, ?, ?)");

            statement1.setLong(1, entity.getId());
            statement1.setString(2, entity.getName());
            statement1.setString(3, entity.getDescriere());
            statement1.setTimestamp(4, Timestamp.valueOf(entity.getData()));

            statement1.executeUpdate();

            List<Long> recipients = entity.getParticipanti();
            PreparedStatement statement2 = this.connection.prepareStatement("INSERT INTO utilizatoreveniment(idutilizator, ideveniment) VALUES (?, ?)");
            for(Long recipient : recipients)
            {
                statement2.setLong(1, recipient);
                statement2.setLong(2, entity.getId());

                statement2.executeUpdate();
            }
        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return entity;
    }

    @Override
    public Event delete(Long aLong) {
        return null;
    }

    @Override
    public Event update(Event entity) {
        return null;
    }

    @Override
    public Page<Event> findAll(Pageable pageable) {
        Set<Event> events = new HashSet<>();
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM eveniment LIMIT ? OFFSET ?");
            statement.setInt(1,pageable.getPageSize());
            statement.setInt(2,pageable.getPageSize() * pageable.getPageNumber());
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next())
            {
                long idEvent = resultSet.getLong("id");
                String nume = resultSet.getString("name");
                String descriere = resultSet.getString("descriere");
                LocalDateTime date = resultSet.getTimestamp("data").toLocalDateTime();

                PreparedStatement statement2 = this.connection.prepareStatement("SELECT * FROM utilizatoreveniment WHERE ideveniment = (?)");
                statement2.setLong(1, idEvent);
                ResultSet resultSet2 = statement2.executeQuery();

                ArrayList<Long> participanti = new ArrayList<>();
                while(resultSet2.next())
                {
                    participanti.add(resultSet2.getLong("idutilizator"));
                }

                Event event = new Event(nume, descriere, date, participanti);
                event.setId(idEvent);

                events.add(event);
            }
        }catch (SQLException e)
        {
            e.printStackTrace();
        }

        return new PageImplementation<>(pageable, events.stream());
    }

    public Page<Event> findAllForUser(Long id, Pageable pageable) {
        Set<Event> events = new HashSet<>();
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM utilizatoreveniment WHERE idutilizator = (?) LIMIT ? OFFSET ?");
            statement.setLong(1, id);
            statement.setInt(2,pageable.getPageSize());
            statement.setInt(3,pageable.getPageSize() * pageable.getPageNumber());
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next())
            {
                long idUtilizator = resultSet.getLong("idutilizator");
                long idEveniment = resultSet.getLong("ideveniment");

                events.add(findOne(idEveniment));
            }

        }catch (SQLException e)
        {
            e.printStackTrace();
        }

        return new PageImplementation<>(pageable, events.stream());
    }

    public Iterable<Event> getAllForUser(Long id) {
        Set<Event> events = new HashSet<>();
        try
        {
            PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM utilizatoreveniment WHERE idutilizator = (?) AND notificari = 'ON'");
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next())
            {
                long idUtilizator = resultSet.getLong("idutilizator");
                long idEveniment = resultSet.getLong("ideveniment");

                events.add(findOne(idEveniment));
            }

        }catch (SQLException e)
        {
            e.printStackTrace();
        }
        return events;
    }
}
