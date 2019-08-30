package iti.commons.maven.fetcher;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.DependencyNode;

/**
 * @author ITI
 * Created by ITI on 25/03/19
 */
public class MavenFetchResult {

   private final CollectResult result;

    MavenFetchResult(CollectResult result) {
       this.result = result;
   }


   public boolean hasErrors() {
        return result.getExceptions().isEmpty();
   }



    private String collectResultTree() {
        StringBuilder string = new StringBuilder();
        if (result.getRoot().getArtifact() == null) {
            for (DependencyNode child : result.getRoot().getChildren()) {
                collectResultTree(child, string, 0);
            }
        } else {
            collectResultTree(result.getRoot(), string, 0);
        }
        return string.toString();
    }



    private StringBuilder collectResultTree(DependencyNode node, StringBuilder string, int level) {
        for (int i = 0; i < level; i++) {
            string.append("    ");
        }
        if (level > 0) {
            string.append("+-");
        }
        if (node.getArtifact() != null) {
            Artifact artifact = node.getArtifact();
            string
                    .append(artifact.getGroupId())
                    .append(':')
                    .append(artifact.getArtifactId())
                    .append(':')
                    .append(artifact.getVersion())
                    .append("\n");
        } for (DependencyNode child : node.getChildren()) {
            collectResultTree(child, string, level+1);
        }
        return string;
    }

}
