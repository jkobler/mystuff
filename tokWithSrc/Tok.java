import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Panel;
import java.awt.Graphics;
import java.awt.MenuBar;
import java.awt.Menu;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Button;
import java.awt.CheckboxMenuItem;
import java.util.Vector;
//import java.awt.Dialog;
import java.util.StringTokenizer;
/*
 * Created on Dec 15, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author Jonathan Kobler
 *
 */
public class Tok
  extends Frame
  implements MouseListener, ActionListener, WindowListener, ItemListener
{
  
  Panel pnl_tok;
  MenuBar mnb_top;
  Menu mnu_game;
  Menu mnu_help;
  Menu mnu_size;
  Menu mnu_sizeRows;
  Menu mnu_sizeCols;
  Button btn_remove;
  int rectWidth = 20;
  int rectHeight = 20;
  Vector vtr_pieces;
  Color colorOrd[];
  Color selectedColorOrd[];
  Color fontColorOrd[];
  int xOffset = 10;
  int yOffset = 47;
  
  int currentNumber = -1;
  int countSelected = 0;
  
  int rowSize=10;
  int colSize=10;
  
  boolean isDebug = false;
  
  boolean isInApp;
  
  boolean sidesOnly = false;
  
  boolean useRectangles = true;
  
  StringBuffer sb_stats;
  int score = 0;
  
  CheckboxMenuItem cmn_sidesOnly;
  CheckboxMenuItem cmn_circles;
  
  CheckboxMenuItem cmn_rows10;
  CheckboxMenuItem cmn_rows20;
  CheckboxMenuItem cmn_rows50;
  CheckboxMenuItem cmn_rows100;

  CheckboxMenuItem cmn_cols10;
  CheckboxMenuItem cmn_cols20;
  CheckboxMenuItem cmn_cols50;
  CheckboxMenuItem cmn_cols100;

  int btnHeight= 20;
  
  TokHelp dlg_help;
  TokHelp dlg_about;
  
  
  public static void main(String[] args)
  {
    Tok tok = new Tok(GraphicsEnvironment.getLocalGraphicsEnvironment(), false);
    if (args.length > 0 && args[0].equals("debug")) {
      tok.setDebug(true);
    }
  }

  /**
   * @param gc
   */
  public Tok(GraphicsEnvironment ge, boolean isInApp)
  {
    super("Tok",ge.getDefaultScreenDevice().getDefaultConfiguration());
    this.isInApp = isInApp;
    vtr_pieces = new Vector(100);
    colorOrd = new Color[8];
    selectedColorOrd = new Color[8];
    this.setResizable(false);

    sb_stats = new StringBuffer("100/100");
    colorOrd[0] = new Color(200,200,200);
    colorOrd[1] = new Color(200,0,0);
    colorOrd[2] = new Color(225,128,0);
    colorOrd[3] = new Color(200,200,0);
    colorOrd[4] = new Color(0,200,0);
    colorOrd[5] = new Color(0,0,200);
    colorOrd[6] = new Color(200,0,200);
    colorOrd[7] = new Color(128,128,128);

    selectedColorOrd[0] = colorOrd[0].brighter();
    selectedColorOrd[1] = colorOrd[1].brighter();
    selectedColorOrd[2] = colorOrd[2].brighter();
    selectedColorOrd[3] = colorOrd[3].brighter();
    selectedColorOrd[4] = colorOrd[4].brighter();
    selectedColorOrd[5] = colorOrd[5].brighter();
    selectedColorOrd[6] = colorOrd[6].brighter();
    selectedColorOrd[7] = colorOrd[7].brighter();
     
    fontColorOrd = new Color[8];
    fontColorOrd[0] = Color.BLACK;
    fontColorOrd[1] = Color.WHITE;
    fontColorOrd[2] = Color.BLACK;
    fontColorOrd[3] = Color.BLACK;
    fontColorOrd[4] = Color.BLACK;
    fontColorOrd[5] = Color.WHITE;
    fontColorOrd[6] = Color.WHITE;
    fontColorOrd[7] = Color.BLACK;
    
    setBackground(Color.BLACK);
    
    mnu_help = new Menu("Help");
    mnu_help.add("How To Play");
    mnu_help.add("About");

    cmn_rows10 = new CheckboxMenuItem("10 Rows",true);
    cmn_rows20 = new CheckboxMenuItem("20 Rows",false);
    cmn_rows50 = new CheckboxMenuItem("30 Rows",false);

    cmn_cols10 = new CheckboxMenuItem("10 Columns",true);
    cmn_cols20 = new CheckboxMenuItem("20 Columns",false);
    cmn_cols50 = new CheckboxMenuItem("30 Columns",false);

    mnu_size = new Menu("Size");
    mnu_sizeRows = new Menu("Rows");
    mnu_sizeCols = new Menu("Columns");

    mnu_sizeRows.add(cmn_rows10);
    mnu_sizeRows.add(cmn_rows20);
    mnu_sizeRows.add(cmn_rows50);
    
    mnu_sizeCols.add(cmn_cols10);
    mnu_sizeCols.add(cmn_cols20);
    mnu_sizeCols.add(cmn_cols50);
    
    mnu_size.add(mnu_sizeRows);
    mnu_size.add(mnu_sizeCols);

    mnu_game = new Menu("Game");
    mnu_game.add("Reset");
    cmn_sidesOnly = new CheckboxMenuItem("Must have exposed sides",false);
    cmn_circles = new CheckboxMenuItem("Circles",false);
    mnu_game.add(cmn_sidesOnly);
    mnu_game.add(cmn_circles);
    mnu_game.add(mnu_size);
    mnu_game.add("-");
    mnu_game.add("Quit");
    
    mnb_top = new MenuBar();
    mnb_top.add(mnu_game);
    mnb_top.add(mnu_help);
    
    setMenuBar(mnb_top);
    dlg_help = new TokHelp(this, ge.getDefaultScreenDevice().getDefaultConfiguration());
    dlg_about = new TokHelp(this, ge.getDefaultScreenDevice().getDefaultConfiguration());
    dlg_about.setText("                 Tok by Jonathan Kobler\n\n                      December 2005");
    
    
    btn_remove = new Button("Remove");
    
    btn_remove.setFont(new Font("San Serif",Font.BOLD,12));
    
    add(btn_remove);
    
    setLayout(null);
    FontMetrics fm = this.getFontMetrics(btn_remove.getFont());
    btnHeight = (int) Math.round(fm.getHeight()*1.25);
    btn_remove.setSize((int) Math.round(fm.stringWidth("Remove")*1.25),btnHeight);
    
    
    this.setSize((rectWidth*rowSize)+xOffset+10,(rectHeight*colSize)+yOffset+10+btnHeight);
    
    btn_remove.setLocation(5,(rectHeight*colSize)+yOffset+5);
    
    this.addWindowListener(this);
    this.addMouseListener(this);
    mnu_game.addActionListener(this);
    mnu_help.addActionListener(this);
    btn_remove.addActionListener(this);
    cmn_sidesOnly.addItemListener(this);
    cmn_rows10.addItemListener(this);
    cmn_rows20.addItemListener(this);
    cmn_rows50.addItemListener(this);
    cmn_cols10.addItemListener(this);
    cmn_cols20.addItemListener(this);
    cmn_cols50.addItemListener(this);
    cmn_circles.addItemListener(this);
    reset();
    if (!isInApp) {
      this.show();
    }
    debugPrint("Started");
  }
  
  public void show() {
    super.show();
    repaint();
  }
  
  public void repaint() {
    paint(getGraphics());
  }


  public void paint(Graphics g) {
    super.paint(g);
    int x, y;
     /*
      * 0 - Color.WHITE
      * 1 - Color.RED
      * 2 - Color.ORANGE
      * 3 - Color.YELLOW
      * 4 - Color.GREEN
      * 5 - Color.BLUE
      * 6 - Color.MAGENTA
      * 7 - Color.GRAY - wildcard
      */
///colSize      
    FontMetrics fm = this.getFontMetrics(new Font("Monospaced",Font.BOLD,14));
    g.setFont(new Font("Monospaced",Font.BOLD,14));
    g.clearRect(xOffset,yOffset,this.getSize().width, this.getSize().height);
    for (y=0;y<colSize;y++) {
      for (x=0;x<rowSize;x++) {
/*        if (isDebug) {
          System.out.print("x=");
          System.out.print(x);
          System.out.print("; y=");
          System.out.print(y);
          System.out.print("; number=");
          System.out.print(getPiece(x,y).number);
          System.out.print("; isOn=");
          System.out.print(getPiece(x,y).isOn);
          System.out.print("; isSelectable=");
          System.out.print(getPiece(x,y).isSelectable);
          System.out.print("; isSelected=");
          System.out.println(getPiece(x,y).isSelected);
        }*/
        if (getPiece(x,y).isOn) {
          String strNum = String.valueOf(getPiece(x,y).number);
          if (getPiece(x,y).isSelected) {
            g.setColor(selectedColorOrd[getPiece(x,y).number]);
            if (useRectangles) {
              g.fillRect(x*rectWidth+xOffset,y*rectHeight+yOffset,rectWidth,rectHeight);
              g.setColor(Color.BLACK);
              g.drawRect(x*rectWidth+xOffset+1,y*rectHeight+yOffset+1,rectWidth-3,rectHeight-3);
            }
            else {
              g.fillOval(x*rectWidth+xOffset,y*rectHeight+yOffset,rectWidth,rectHeight);
              g.setColor(Color.BLACK);
              g.drawOval(x*rectWidth+xOffset+1,y*rectHeight+yOffset+1,rectWidth-3,rectHeight-3);
            }
          }
          else {
            g.setColor(colorOrd[getPiece(x,y).number]);
            if (useRectangles) {
              g.fill3DRect(x*rectWidth+xOffset,y*rectHeight+yOffset,rectWidth,rectHeight,true);
            }
            else {
              g.fillOval(x*rectWidth+xOffset,y*rectHeight+yOffset,rectWidth,rectHeight);
              g.setColor(colorOrd[getPiece(x,y).number].darker());
              g.fillOval(x*rectWidth+xOffset+1,y*rectHeight+yOffset+1,rectWidth-1,rectHeight-1);
              g.setColor(colorOrd[getPiece(x,y).number].brighter());
              g.fillOval(x*rectWidth+xOffset,y*rectHeight+yOffset,rectWidth-1,rectHeight-1);
              g.setColor(colorOrd[getPiece(x,y).number]);
              g.fillOval(x*rectWidth+xOffset+1,y*rectHeight+yOffset+1,rectWidth-2,rectHeight-2);
            }
          }
          g.setColor(fontColorOrd[getPiece(x,y).number]);
          int tx = (20-fm.stringWidth(strNum))/2 + x*rectWidth+xOffset;
          int ty = fm.getHeight() + y*rectHeight - rectHeight/4+yOffset;
          g.drawString(strNum,tx,ty);
          
        }
      }
    }
    g.setColor(Color.WHITE);
    int tx = this.getSize().width-fm.stringWidth(sb_stats.toString())-xOffset;
    int ty = this.getSize().height - (fm.getHeight()/2);
    g.drawString(sb_stats.toString(),tx,ty);
    
  }
  
  public void reset() {
    int x, y;
    currentNumber = -1;
    score = 0;
    vtr_pieces.clear();
    for (y=0;y<colSize;y++) {
      for (x=0;x<rowSize;x++) {
        vtr_pieces.add(new Piece(getRandom(),true,false, false));
        if ((y == 0 || y == colSize-1) && !sidesOnly) {
          ((Piece) vtr_pieces.lastElement()).isSelectable=true;
        }
        else {
          ((Piece) vtr_pieces.lastElement()).isSelectable=false;
        }
        if (x == 0 || x == rowSize-1) {
          ((Piece) vtr_pieces.lastElement()).isSelectable=true;
        }
      }
    }
    this.setSize((rectWidth*rowSize)+xOffset+10,(rectHeight*colSize)+yOffset+10+btnHeight);
    btn_remove.setLocation(5,(rectHeight*colSize)+yOffset+5);
    sb_stats = new StringBuffer(String.valueOf(vtr_pieces.size()));
    sb_stats.append("/");
    sb_stats.append(vtr_pieces.size());
  }
  
  public int getRandom() {
    return((int) (Math.random()*100)%8);
  }
  
  void close() {
    if (isInApp) {
      this.hide();
    }
    else {
      System.exit(0);
    }
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e)
  {
//    System.out.println(e.toString());
    int xclk = (e.getX()-xOffset)/rectWidth;
    int yclk = (e.getY()-yOffset)/rectHeight;
    boolean needsRepaint = false;
//    System.out.println("xclk = " + xclk + "; yclk = " + yclk);
    if (isDebug && yclk < colSize && xclk < rowSize) {
      System.out.print("xclk=");
      System.out.print(xclk);
      System.out.print("; yclk=");
      System.out.print(yclk);
      System.out.print("; number=");
      System.out.print(getPiece(xclk,yclk).number);
      System.out.print("; isOn=");
      System.out.print(getPiece(xclk,yclk).isOn);
      System.out.print("; isSelectable=");
      System.out.print(getPiece(xclk,yclk).isSelectable);
      System.out.print("; isSelected=");
      System.out.println(getPiece(xclk,yclk).isSelected);
      System.out.print("e.getX()=");
      System.out.print(e.getX());
      System.out.print("; xOffset=");
      System.out.print(xOffset);
      System.out.print("; e.getY()=");
      System.out.print(e.getY());
      System.out.print("; yOffset=");
      System.out.println(yOffset);
    }
    if (e.getX() > xOffset && e.getY() > yOffset
    && e.getX() < xOffset+(rectWidth*rowSize) 
    && e.getY() < yOffset+(rectHeight*colSize)) {
    
      if (getPiece(xclk,yclk).isSelectable && getPiece(xclk,yclk).isOn) {
        if (getPiece(xclk,yclk).isSelected) {
          getPiece(xclk,yclk).isSelected=false;
          if (countSelected > 0) countSelected--;
          needsRepaint = true;

        }
        else if (getCurrentNumber() == getPiece(xclk,yclk).number || getCurrentNumber() == -1) {
          getPiece(xclk,yclk).isSelected=true;
          needsRepaint = true;
          if (getCurrentNumber() == -1 ) {
            setCurrentNumber(getPiece(xclk,yclk).number);
          }
          countSelected++;
        }
      }
      if (countSelected == 0) {
        setCurrentNumber(-1);
        
      }
      if (isDebug) {
        System.out.print("isSelected=");
        System.out.println(getPiece(xclk,yclk).isSelected);
      } 
      if (isDebug) System.out.println("countSelected="+countSelected);
      if (isDebug) System.out.println("needsRepaint="+needsRepaint);
      if (isDebug) System.out.println("getCurrentNumber()="+getCurrentNumber());
    
      if (needsRepaint) repaint();
    }

  }
  public void mouseEntered(MouseEvent e)
  {
  }
  public void mouseExited(MouseEvent e)
  {
  }
  public void mousePressed(MouseEvent e)
  {
  }
  public void mouseReleased(MouseEvent e)
  {
  }

  public void actionPerformed(ActionEvent e)
  {
    
    if (e.getActionCommand().equals("Quit")){
      close();
    }
    else if (e.getActionCommand().equals("About")){
      dlg_about.show();
    }
    else if (e.getActionCommand().equals("How To Play")){
      dlg_help.show();
    }
    else if (e.getActionCommand().equals("Help OK")){
      dlg_help.hide();
      dlg_about.hide();
    }
    else if (e.getActionCommand().equals("Reset")){
      reset();
      repaint();
    }
    else if (e.getActionCommand().equals("Remove") && countSelected > 1 ){
      int y, x;
      int count = 0;
      float tempScore = 1;
      double scoreMulti = 1.5;
      if (sidesOnly) scoreMulti = 1.75;
      for (y=0;y<colSize;y++) {
        for (x=0;x<rowSize;x++) {
          if (isDebug) {
            System.out.print("x=");
            System.out.print(x);
            System.out.print("; y=");
            System.out.print(y);
            System.out.print("; number=");
            System.out.print(getPiece(x,y).number);
            System.out.print("; isOn=");
            System.out.print(getPiece(x,y).isOn);
            System.out.print("; isSelectable=");
            System.out.print(getPiece(x,y).isSelectable);
            System.out.print("; isSelected=");
            System.out.println(getPiece(x,y).isSelected);
          }
          if (getPiece(x,y).isSelected && getPiece(x,y).isOn) {
            tempScore *= scoreMulti;
            getPiece(x,y).isOn=false;
            if (y>0) {
              if (!sidesOnly) getPiece(x,y-1).isSelectable= true;
            }
            if (y<colSize-1){
              if(!sidesOnly) getPiece(x,y+1).isSelectable= true;
            }
            
            if (x>0) getPiece(x-1,y).isSelectable= true;
            if (x<rowSize-1) getPiece(x+1,y).isSelectable= true;
            
          }
          if (isDebug) {
            System.out.print("isSelectable=");
            System.out.println(getPiece(x,y).isSelectable);
          }
          
          if (getPiece(x,y).isOn) {
            count++;
          }
        }
        if (isDebug) {
          System.out.print("tempScore=");
          System.out.print(tempScore);
          System.out.print("; count=");
          System.out.println(count);
        }
      }
      score += Math.ceil(tempScore); 
      if (count == 0) {
        score += 200 * (vtr_pieces.size()/100);
      }
      sb_stats = new StringBuffer(String.valueOf(score));
      sb_stats.append("  ");
      sb_stats.append(count);
      sb_stats.append("/");
      sb_stats.append(vtr_pieces.size());
      currentNumber = -1;
      countSelected = 0;
    }
    repaint();

  }

  public void windowActivated(WindowEvent e)
  {
  }

  public void windowClosed(WindowEvent e)
  {
    
  }

  public void windowClosing(WindowEvent e)
  {
    close();
 
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
  public void itemStateChanged(ItemEvent e) {
    String item = e.getItem().toString();
    if (item.equals("Must have exposed sides") 
    && sidesOnly != cmn_sidesOnly.getState()) {
      this.sidesOnly = this.cmn_sidesOnly.getState();  
      reset();  
      repaint();
    }
    else if (item.equals("Circles")) {
      this.useRectangles = (! cmn_circles.getState());
      repaint();
    }
    else if (item.endsWith("Rows")) {
      StringTokenizer st = new StringTokenizer(item);
      this.rowSize = Integer.parseInt(st.nextToken());
      int i;
      for (i=0;i<mnu_sizeRows.getItemCount();i++) {
        if (((CheckboxMenuItem) mnu_sizeRows.getItem(i)).getLabel().equals(item)) {
          ((CheckboxMenuItem) mnu_sizeRows.getItem(i)).setState(true);
        }
        else {
          ((CheckboxMenuItem) mnu_sizeRows.getItem(i)).setState(false);
        }
        
      }
      reset();  
      repaint();
    }
    else if (item.endsWith("Columns")) {
      StringTokenizer st = new StringTokenizer(item);
      this.colSize = Integer.parseInt(st.nextToken());
      int i;
      for (i=0;i<mnu_sizeCols.getItemCount();i++) {
        if (((CheckboxMenuItem) mnu_sizeCols.getItem(i)).getLabel().equals(item)) {
          ((CheckboxMenuItem) mnu_sizeCols.getItem(i)).setState(true);
        }
        else {
          ((CheckboxMenuItem) mnu_sizeCols.getItem(i)).setState(false);
        }
        
      }
      reset();  
      repaint();
      
    }
  }
  public synchronized Piece getPiece(int x, int y) {
    return((Piece) vtr_pieces.elementAt((y*this.rowSize)+x));
  }
  /**
   * @return
   */
  public synchronized int getCurrentNumber()
  {
    return currentNumber;
  }

  /**
   * @param i
   */
  public synchronized void setCurrentNumber(int i)
  {
    currentNumber = i;
  }



  public class Piece {
    int number;
    boolean isOn;
    boolean isSelected;
    boolean isSelectable;
    public Piece (int pieceNumber,boolean pieceIsOn,boolean pieceIsSelected, boolean pieceIsSelectable) {
      this.number=pieceNumber;
      this.isOn=pieceIsOn;
      this.isSelected=pieceIsSelected;
      this.isSelectable=pieceIsSelectable;
      
    }
  }
  /**
   * @return
   */
  public boolean isDebug()
  {
    return isDebug;
  }

  /**
   * @param b
   */
  public void setDebug(boolean b)
  {
    isDebug = b;
  }
  
  public void debugPrint(String s) {
    if (isDebug) System.out.println(s);
  }

}
