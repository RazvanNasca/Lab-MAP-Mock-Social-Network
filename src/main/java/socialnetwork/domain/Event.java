package socialnetwork.domain;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Event extends Entity<Long>
{
    private String name;
    private String descriere;
    private LocalDateTime data;
    private ArrayList<Long> participanti;

    public Event(String name, String descriere, LocalDateTime data, ArrayList<Long> participanti)
    {
        this.name = name;
        this.descriere = descriere;
        this.data = data;
        this.participanti = participanti;
    }

    public String getName(){return name;}
    public String getDescriere(){return descriere;}
    public LocalDateTime getData(){return data;}
    public ArrayList<Long> getParticipanti(){return participanti;}
    public void setName(String name1){name = name1;}
    public void setDescriere(String descriere1){descriere = descriere1;}
    public void setData(LocalDateTime data1){data = data1;}
    public void setParticipanti(ArrayList<Long> participanti1){participanti = participanti1;}

}
