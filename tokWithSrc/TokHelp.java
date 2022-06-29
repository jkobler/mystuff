import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Button;
import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * @author Jonathan Kobler
 *
 */
public class TokHelp extends Dialog implements WindowListener
{
  Button btn_ok;
  TextArea txa_help;

  public TokHelp(Frame owner, GraphicsConfiguration gc)
  {
    super(owner, "Tok Help",  true, gc);
    
    StringBuffer sb = new StringBuffer("Select two or more tiles of the ");
    sb.append("same color or with the same number and press the Remove button. ");
    sb.append("Your score will be increased depending upon the number of tiles ");
    sb.append("removed each press of the Remove button. The points are calculated ");
    sb.append("exponentially so it definitely pays to get the most tiles removed in ");
    sb.append("one fell swoop.\n\n Only tiles with an open edge can be selected. For a ");
    sb.append("real challenge, try the \"Must have exposed sides\" game. You will ");
    sb.append("only be able to select tiles with left or right sides exposed.\n\n ");
    sb.append("You may also select row and column size for real time killing fun.\n\n ");
    sb.append("Additional points are awarded for clearing the board. ");
    
    txa_help = new TextArea(sb.toString(), 10,35,TextArea.SCROLLBARS_VERTICAL_ONLY);
    txa_help.setEditable(false);
    
    btn_ok = new Button("OK");
    btn_ok.setActionCommand("Help OK");
    add(txa_help,BorderLayout.NORTH);
    add(btn_ok,BorderLayout.SOUTH);
    pack();
    btn_ok.addActionListener((ActionListener) owner);
    this.addWindowListener(this);
  }
  
  public void setText(String s) {
    txa_help.setText(s);
  }
  
  public void show() {
    this.setLocationRelativeTo(this.getOwner());
    super.show();
  }
  
  public void windowActivated(WindowEvent e)
  {
  }

  public void windowClosed(WindowEvent e)
  {
    
  }

  public void windowClosing(WindowEvent e)
  {
    hide();
 
  }
  public void windowDeactivated(WindowEvent e)
  {
  }

  public void windowDeiconified(WindowEvent e)
  {
  }

  public void windowIconified(WindowEvent e)
  {
  }

  public void windowOpened(WindowEvent e)
  {
  }

}
