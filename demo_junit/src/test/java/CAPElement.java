import com.oracle.javacard.ams.config.CAPFile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CAPElement {
    private String aid;
    private String packageAid;
    private String classAid;
    private CAPFile capFile;
    private boolean toInstall;
}
