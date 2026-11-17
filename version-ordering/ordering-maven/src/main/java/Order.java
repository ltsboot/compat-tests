import java.nio.file.*;
import org.apache.maven.artifact.versioning.ComparableVersion;

/** Prints "A OP B" for every line of pairs.txt using Maven's real ComparableVersion. */
public class Order {
	public static void main(String[] args) throws Exception {
		for (String line : Files.readAllLines(Paths.get(args[0]))) {
			if (line.trim().isEmpty() || line.startsWith("#")) continue;
			String[] p = line.trim().split("\\s+");
			int c = new ComparableVersion(p[0]).compareTo(new ComparableVersion(p[1]));
			System.out.println("RESULT " + p[0] + " " + (c > 0 ? ">" : c < 0 ? "<" : "=") + " " + p[1]);
		}
	}
}
