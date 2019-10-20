/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.gherkin.parser;


import java.util.List;

import gherkin.ast.Comment;


public interface CommentedNode {

    List<Comment> getComments();
}
