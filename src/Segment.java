import java.io.Serializable;

public class Segment implements Serializable {
    enum SegmentType {
        message,
        connectionRequest,
        connectionAck;
    }


    private SegmentType type;
    private String name;
    private String message;
    private static final long serialVersionUID = 1L;

    public Segment() {
    }

    public Segment(SegmentType type, String name) {
        this.type = type;
        this.name = name;
    }

    public Segment(SegmentType type, String name, String message) {
        this.type = type;
        this.name = name;
        this.message = message;
    }

    public SegmentType getType() {
        return type;
    }

    public void setType(SegmentType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}