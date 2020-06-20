package Client.Sprites;

public class Live extends Sprite {

    public Integer id;
    public Boolean taken;

    public Live() {

    }

    public Boolean isTaken() {
        return taken;
    }

    public void setTaken(Boolean taken) {
        this.taken = taken;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
