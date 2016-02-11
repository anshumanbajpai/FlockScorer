package co.flock.redis.domain;

public class User implements DomainObject {

    public static final String OBJECT_KEY = "USER";
    private String name;
    private int score;

    public User() {
    }

    public User(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }

    @Override
    public String getKey() {
        return getName();
    }

    @Override
    public String getObjectKey() {
        return OBJECT_KEY;
    }
}
