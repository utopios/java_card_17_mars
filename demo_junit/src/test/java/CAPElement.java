import com.oracle.javacard.ams.config.CAPFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;

@Data

public class CAPElement {
    private String aid;
    private String packageAid;
    private String classAid;
    private String pathCapFile;
    private CAPFile capFile;
    private boolean toInstall;

    public CAPElement(String aid, String packageAid, String classAid, String pathCapFile, boolean toInstall) throws IOException {
        this.aid = aid;
        this.packageAid = packageAid;
        this.classAid = classAid;
        this.pathCapFile = pathCapFile;
        this.toInstall = toInstall;
        this.capFile = CAPFile.from(pathCapFile);
    }
}
