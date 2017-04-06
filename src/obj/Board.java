package obj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by chengle on 16/10/3.
 */
public class Board extends JPanel implements MouseListener{
    public static final int MARGIN = 30;
    public static final int GRID_SPAN = 35;
    public static final int ROWS = 15;
    public static final int COLS = 15;
    public static final int CUT = 3;
    public static int SEARCH_DEPTH = 7;

    Point[] chessList = new Point[(ROWS+1)*(COLS+1)];
    int[][] pointType = new int[ROWS+1][COLS+1];  //0 empty; 1 black; 2 white

    boolean isBlack = true; // black goes first
    boolean gameOver = false;
    int chessCount;
    int xIndex,yIndex;
//    int maxXIndex = 0,minXIndex = COLS;
//    int maxYIndex = 0,minYIndex = ROWS;
    int maxXIndex = COLS,minXIndex = 0;
    int maxYIndex = ROWS,minYIndex = 0;

    Map<String,Integer> evaluateList = new HashMap<String,Integer>();  //0:empty 1:black 2:white

    Image img;
    Image shadows;
    Color colortemp;

    public Board(){
        img = Toolkit.getDefaultToolkit().getImage("/Users/chengle/IdeaProjects/Gobang/src/res/board1.jpg");
        evaluateList.put("22",2);
        evaluateList.put("222",8);
        evaluateList.put("2222",30);
        evaluateList.put("22222",1000);
        evaluateList.put("21",1);
        evaluateList.put("211",6);
        evaluateList.put("2111",20);
        evaluateList.put("21111",70);
        evaluateList.put("2022",7);
        evaluateList.put("20222",25);
        evaluateList.put("2011",4);
        addMouseListener(this);
        addMouseMotionListener(new MouseMotionListener(){
            public void mouseDragged(MouseEvent e){

            }

            public void mouseMoved(MouseEvent e){
                int x1=(e.getX()-MARGIN+GRID_SPAN/2)/GRID_SPAN;
                //将鼠标点击的坐标位置转成网格索引
                int y1=(e.getY()-MARGIN+GRID_SPAN/2)/GRID_SPAN;
                //游戏已经结束不能下
                //落在棋盘外不能下
                //x，y位置已经有棋子存在，不能下
                if(x1<0||x1>ROWS||y1<0||y1>COLS||gameOver||findChess(x1,y1))
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    //设置成默认状态
                else setCursor(new Cursor(Cursor.HAND_CURSOR));

            }
        });


    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        int imgWidth = img.getWidth(this);
        int imgHeight = img.getHeight(this);
        int FWidth = getWidth();
        int FHeight = getHeight();
        int x = (FWidth-imgWidth)/2;
        int y = (FHeight-imgHeight)/2;
        g.drawImage(img,x,y,null);

        for(int i=0;i<=ROWS;i++){
            g.drawLine(MARGIN, MARGIN+i*GRID_SPAN, MARGIN+COLS*GRID_SPAN, MARGIN+i*GRID_SPAN);
        }

        for(int i=0;i<=COLS;i++){
            g.drawLine(MARGIN+i*GRID_SPAN, MARGIN, MARGIN+i*GRID_SPAN, MARGIN+ROWS*GRID_SPAN);
        }

        for(int i=0;i<chessCount;i++){
            //网格交叉点x，y坐标
            int xPos=chessList[i].getX()*GRID_SPAN+MARGIN;
            int yPos=chessList[i].getY()*GRID_SPAN+MARGIN;
            g.setColor(chessList[i].getColor());//设置颜色
            // g.fillOval(xPos-Point.DIAMETER/2, yPos-Point.DIAMETER/2,
            //Point.DIAMETER, Point.DIAMETER);
            //g.drawImage(shadows, xPos-Point.DIAMETER/2, yPos-Point.DIAMETER/2, Point.DIAMETER, Point.DIAMETER, null);
            colortemp=chessList[i].getColor();
            if(colortemp==Color.black){
                RadialGradientPaint paint = new RadialGradientPaint(xPos-Point.DIAMETER/2+25, yPos-Point.DIAMETER/2+10, 20, new float[]{0f, 1f}
                        , new Color[]{Color.WHITE, Color.BLACK});
                ((Graphics2D) g).setPaint(paint);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);

            }
            else if(colortemp==Color.white){
                RadialGradientPaint paint = new RadialGradientPaint(xPos-Point.DIAMETER/2+25, yPos-Point.DIAMETER/2+10, 70, new float[]{0f, 1f}
                        , new Color[]{Color.WHITE, Color.BLACK});
                ((Graphics2D) g).setPaint(paint);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);

            }

            Ellipse2D e = new Ellipse2D.Float(xPos-Point.DIAMETER/2, yPos-Point.DIAMETER/2, 34, 35);
            ((Graphics2D) g).fill(e);
            //标记最后一个棋子的红矩形框

            if(i==chessCount-1){//如果是最后一个棋子
                g.setColor(Color.red);
                g.drawRect(xPos-Point.DIAMETER/2, yPos-Point.DIAMETER/2,
                        34, 35);
            }
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e){//鼠标在组件上按下时调用

        //游戏结束时，不再能下
        if(gameOver) return;

        String colorName=isBlack?"黑棋":"白棋";

        //将鼠标点击的坐标位置转换成网格索引
        xIndex=(e.getX()-MARGIN+GRID_SPAN/2)/GRID_SPAN;
        yIndex=(e.getY()-MARGIN+GRID_SPAN/2)/GRID_SPAN;

        //落在棋盘外不能下
        if(xIndex<0||xIndex>ROWS||yIndex<0||yIndex>COLS)
            return;

        //如果x，y位置已经有棋子存在，不能下
        if(findChess(xIndex,yIndex))return;

        //可以进行时的处理
        Point ch=new Point(xIndex,yIndex,isBlack?Color.black:Color.white);
        chessList[chessCount++]=ch;
        pointType[xIndex][yIndex] = 1;
        repaint();//通知系统重新绘制

        //updateValidBorad();
        //如果胜出则给出提示信息，不能继续下棋

        if(isWin()){
            String msg=String.format("恭喜，%s赢了！", colorName);
            JOptionPane.showMessageDialog(this, msg);
            gameOver=true;
        }
        isBlack=!isBlack;
        stepByPC();
        //updateValidBorad();
    }

    public void updateValidBorad(){
        if(xIndex<=minXIndex){
            if(xIndex<=CUT)
                minXIndex = 0;
            else
                minXIndex = xIndex - CUT;
        }
        if(xIndex>=maxXIndex){
            if(xIndex>=COLS-CUT)
                maxXIndex = COLS;
            else
                maxXIndex = xIndex + CUT;
        }
        if(yIndex<=minYIndex){
            if(yIndex<=CUT)
                minYIndex = 0;
            else
                minYIndex = yIndex - CUT;
        }
        if(yIndex>=maxYIndex){
            if(yIndex>=ROWS-CUT)
                maxYIndex = ROWS;
            else
                maxYIndex = yIndex + CUT;
        }
    }

    //random
    public void rand(){
        Random random = new Random();
        while(true){
            xIndex=random.nextInt(maxXIndex)%(maxXIndex-minXIndex+1)+minXIndex;
            yIndex=random.nextInt(maxYIndex)%(maxYIndex-minYIndex+1)+minYIndex;
            //如果x，y位置已经有棋子存在，不能下
            if(!findChess(xIndex,yIndex)) return;
        }
    }

    //min-max
    public void minMax(){
    }

    public int maxValue(){
        int max=Integer.MIN_VALUE;
        SEARCH_DEPTH--;
        for (int i = minXIndex; i <= maxXIndex; i++) {
            for(int j = minYIndex; j <= maxYIndex; j++){
                if(pointType[i][j]==0){
                    pointType[i][j] = 2;
                    if(SEARCH_DEPTH>0){
                        int v= minValue();
                        if(v>max){
                            max = v;
                            xIndex = i;
                            yIndex = j;
                        }
                    }
                    else{
                        int v = evaluatePoint(i,j,Color.white);
                        if(v>max){
                            max = v;
                            xIndex = i;
                            yIndex = j;
                        }
                    }
                    pointType[i][j] = 0;
                    isBlack=false;
                }
            }
        }
        return max;
    }

    public int minValue(){
        int min = Integer.MAX_VALUE;
        SEARCH_DEPTH--;
        for (int i = minXIndex; i <= maxXIndex; i++) {
            for(int j = minYIndex; j <= maxYIndex; j++){
                if(pointType[i][j]==0){
                    pointType[i][j] = 1;
                    if(SEARCH_DEPTH>0){
                        int v= maxValue();
                        if(v<min){
                            min = v;
                            xIndex = i;
                            yIndex = j;
                        }
                    }
                    else{
                        int v = evaluatePoint(i,j,Color.black);
                        if(v<min){
                            min = v;
                            xIndex = i;
                            yIndex = j;
                        }
                    }
                    pointType[i][j] = 0;
                    isBlack=false;
                }
            }
        }
        return min;
    }

    private void stepByPC(){
        //游戏结束时，不再能下
        if(gameOver) return;

        String colorName=isBlack?"黑棋":"白棋";

        //将鼠标点击的坐标位置转换成网格索引
        //rand();
        maxValue();


        //可以进行时的处理
        Point ch=new Point(xIndex,yIndex,isBlack?Color.black:Color.white);
        chessList[chessCount++]=ch;
        pointType[xIndex][yIndex] = 2;
        repaint();//通知系统重新绘制


        //如果胜出则给出提示信息，不能继续下棋

        if(isWin()){
            String msg=String.format("恭喜，%s赢了！", colorName);
            JOptionPane.showMessageDialog(this, msg);
            gameOver=true;
        }
        isBlack=!isBlack;
    }

    private boolean isWin(){
        int continueCount=1;//连续棋子的个数

        //横向向西寻找
        for(int x=xIndex-1;x>=0;x--){
            Color c=isBlack?Color.black:Color.white;
            if(getChess(x,yIndex,c)!=null){
                continueCount++;
            }else
                break;
        }
        //横向向东寻找
        for(int x=xIndex+1;x<=COLS;x++){
            Color c=isBlack?Color.black:Color.white;
            if(getChess(x,yIndex,c)!=null){
                continueCount++;
            }else
                break;
        }
        if(continueCount>=5){
            return true;
        }else
            continueCount=1;

        //继续另一种搜索纵向
        //向上搜索
        for(int y=yIndex-1;y>=0;y--){
            Color c=isBlack?Color.black:Color.white;
            if(getChess(xIndex,y,c)!=null){
                continueCount++;
            }else
                break;
        }
        //纵向向下寻找
        for(int y=yIndex+1;y<=ROWS;y++){
            Color c=isBlack?Color.black:Color.white;
            if(getChess(xIndex,y,c)!=null)
                continueCount++;
            else
                break;

        }
        if(continueCount>=5)
            return true;
        else
            continueCount=1;


        //继续另一种情况的搜索：斜向
        //东北寻找
        for(int x=xIndex+1,y=yIndex-1;y>=0&&x<=COLS;x++,y--){
            Color c=isBlack?Color.black:Color.white;
            if(getChess(x,y,c)!=null){
                continueCount++;
            }
            else break;
        }
        //西南寻找
        for(int x=xIndex-1,y=yIndex+1;x>=0&&y<=ROWS;x--,y++){
            Color c=isBlack?Color.black:Color.white;
            if(getChess(x,y,c)!=null){
                continueCount++;
            }
            else break;
        }
        if(continueCount>=5)
            return true;
        else continueCount=1;


        //继续另一种情况的搜索：斜向
        //西北寻找
        for(int x=xIndex-1,y=yIndex-1;x>=0&&y>=0;x--,y--){
            Color c=isBlack?Color.black:Color.white;
            if(getChess(x,y,c)!=null)
                continueCount++;
            else break;
        }
        //东南寻找
        for(int x=xIndex+1,y=yIndex+1;x<=COLS&&y<=ROWS;x++,y++){
            Color c=isBlack?Color.black:Color.white;
            if(getChess(x,y,c)!=null)
                continueCount++;
            else break;
        }
        if(continueCount>=5)
            return true;
        else continueCount=1;

        return false;
    }

    private Point getChess(int xIndex,int yIndex,Color color){
        for(Point p:chessList){
            if(p!=null&&p.getX()==xIndex&&p.getY()==yIndex
                    &&p.getColor()==color)
                return p;
        }
        return null;
    }

    //在棋子数组中查找是否有索引为x，y的棋子存在
    private boolean findChess(int x,int y){
        for(Point c:chessList){
            if(c!=null&&c.getX()==x&&c.getY()==y)
                return true;
        }
        return false;
    }

    public void restartGame(){
        //清除棋子
        for(int i=0;i<chessList.length;i++){
            chessList[i]=null;
        }
        //恢复游戏相关的变量值
        isBlack=true;
        gameOver=false; //游戏是否结束
        chessCount =0; //当前棋盘棋子个数
        repaint();
    }

    //悔棋
    public void goback(){
        if(chessCount==0)
            return ;
        chessList[chessCount-1] = null;
        chessList[chessCount-2]=null;
        chessCount -= 2;
        if(chessCount>0){
            xIndex=chessList[chessCount-2].getX();
            yIndex=chessList[chessCount-2].getY();
        }
        //isBlack=!isBlack;
        repaint();
    }

    public Color getType(int x,int y){
        if(pointType[x][y] == 0)
            return null;
        else if(pointType[x][y] == 1)
            return Color.black;
        else
            return Color.white;
    }

//    public int evaluate(){
//        int value = 0;
////        for(Point p :chessList){
////            if(p!=null)
////                value += evaluatePoint(p);
////            else
////                break;
////        }
//        value += evaluatePoint(chessList[chessCount-1]);
//        return value;
//    }

    public int evaluatePoint(int x,int y,Color color){
        int weight = 0;
//        int x = p.getX();
//        int y = p.getY();
//
//        Color color = p.getColor();


        //west
        int[] params1 = new int[5];
        int contiW1 = 0;
        int contiB1 = 0;
        int block1 = 0;
        int blankCount1 = 0;
        int isDenfense1 = 0;
        for(int i=x-1;i>=Math.max(x-4,0);i--){
            Color c = getType(i,y);
            if(c == color){
                if(contiB1>0){
                    isDenfense1 = 1;
                    break;
                }
                contiW1 ++;
            }
            else if(c == null){
                if(contiB1>0||contiW1>0||blankCount1==1)
                    break;
                blankCount1 = 1;
            }
            else{
                block1 = 1;
                if(contiW1>0)
                    break;
                contiB1 ++;
            }
        }

        //east
        int[] params2 = new int[5];
        int contiW2 = 0;
        int contiB2 = 0;
        int block2 = 0;
        int blankCount2 = 0;
        int isDenfense2 = 0;
        for(int i=x+1;i<=Math.min(x+4,COLS);i++){
            Color c = getType(i,y);
            if(c == color){
                if(contiB2>0){
                    isDenfense2 = 1;
                    break;
                }
                contiW2 ++;
            }
            else if(c == null){
                if(contiB2>0||contiW2>0||blankCount2==1)
                    break;
                blankCount2 = 1;
            }
            else{
                block2 = 1;
                if(contiW2>0)
                    break;
                contiB2 ++;
            }
        }

        if(contiW2+x<=3)
            block1 = 1;
        if(contiW1+COLS-x<=3)
            block2 = 1;

        params2[0] = contiW2;
        params2[1] = contiB2;
        params2[2] = blankCount2;
        params2[3] = block2;
        params2[4] = isDenfense2;
        params1[0] = contiW1;
        params1[1] = contiB1;
        params1[2] = blankCount1;
        params1[3] = block1;
        params1[4] = isDenfense1;

        weight += countValue(params1,params2);

        //north
        contiW1 = 0;
        contiB1 = 0;
        block1 = 0;
        blankCount1 = 0;
        isDenfense1 = 0;
        for(int i=y-1;i>=Math.max(y-4,0);i--){
            Color c = getType(x,i);
            if(c == color){
                if(contiB1>0){
                    isDenfense1 = 1;
                    break;
                }
                contiW1 ++;
            }
            else if(c == null){
                if(contiB1>0||contiW1>0||blankCount1==1)
                    break;
                blankCount1 = 1;
            }
            else{
                block1 = 1;
                if(contiW1>0)
                    break;
                contiB1 ++;
            }
        }

        //south
        contiW2 = 0;
        contiB2 = 0;
        block2 = 0;
        blankCount2 = 0;
        isDenfense2 = 0;
        for(int i=y+1;i<=Math.min(y+4,ROWS);i++){
            Color c = getType(x,i);
            if(c == color){
                if(contiB2>0){
                    isDenfense2 = 1;
                    break;
                }
                contiW2 ++;
            }
            else if(c == null){
                if(contiB2>0||contiW2>0||blankCount2==1)
                    break;
                blankCount2 = 1;
            }
            else{
                block2 = 1;
                if(contiW2>0)
                    break;
                contiB2 ++;
            }
        }

        if(contiW2+y<=3)
            block1 = 1;
        if(contiW1+ROWS-y<=3)
            block2 = 1;

        params2[0] = contiW2;
        params2[1] = contiB2;
        params2[2] = blankCount2;
        params2[3] = block2;
        params2[4] = isDenfense2;
        params1[0] = contiW1;
        params1[1] = contiB1;
        params1[2] = blankCount1;
        params1[3] = block1;
        params1[4] = isDenfense1;

        weight += countValue(params1,params2);

        //northeast
        contiW1 = 0;
        contiB1 = 0;
        block1 = 0;
        blankCount1 = 0;
        isDenfense1 = 0;
        for(int i=x+1,j=y-1;j>=Math.max(y-4,0)&&i<=Math.min(x+4,COLS);i++,j--){
            Color c = getType(i,j);
            if(c == color){
                if(contiB1>0){
                    isDenfense1 = 1;
                    break;
                }
                contiW1 ++;
            }
            else if(c == null){
                if(contiB1>0||contiW1>0||blankCount1==1)
                    break;
                blankCount1 = 1;
            }
            else{
                block1 = 1;
                if(contiW1>0)
                    break;
                contiB1 ++;
            }
        }

        //southwest
        contiW2 = 0;
        contiB2 = 0;
        block2 = 0;
        blankCount2 = 0;
        isDenfense2 = 0;
        for(int i=y+1,j=x-1;i<=Math.min(y+4,ROWS)&&j>=Math.max(x-4,0);i++,j--){
            Color c = getType(j,i);
            if(c == color){
                if(contiB2>0){
                    isDenfense2 = 1;
                    break;
                }
                contiW2 ++;
            }
            else if(c == null){
                if(contiB2>0||contiW2>0||blankCount2==1)
                    break;
                blankCount2 = 1;
            }
            else{
                block2 = 1;
                if(contiW2>0)
                    break;
                contiB2 ++;
            }
        }

        if(contiW2+Math.min(COLS-x,y)<=3)
            block1 = 1;
        if(contiW1+Math.min(x,ROWS-y)<=3)
            block2 = 1;

        params2[0] = contiW2;
        params2[1] = contiB2;
        params2[2] = blankCount2;
        params2[3] = block2;
        params2[4] = isDenfense2;
        params1[0] = contiW1;
        params1[1] = contiB1;
        params1[2] = blankCount1;
        params1[3] = block1;
        params1[4] = isDenfense1;

        weight += countValue(params1,params2);

        //northwest
        contiW1 = 0;
        contiB1 = 0;
        block1 = 0;
        blankCount1 = 0;
        isDenfense1 = 0;
        for(int i=x-1,j=y-1;j>=Math.max(y-4,0)&&i>=Math.max(x-4,0);i--,j--){
            Color c = getType(i,j);
            if(c == color){
                if(contiB1>0){
                    isDenfense1 = 1;
                    break;
                }
                contiW1 ++;
            }
            else if(c == null){
                if(contiB1>0||contiW1>0||blankCount1==1)
                    break;
                blankCount1 = 1;
            }
            else{
                block1 = 1;
                if(contiW1>0)
                    break;
                contiB1 ++;
            }
        }

        //southeast
        contiW2 = 0;
        contiB2 = 0;
        block2 = 0;
        blankCount2 = 0;
        isDenfense2 = 0;
        for(int i=y+1,j=x+1;i<=Math.min(y+4,ROWS)&&j<=Math.min(x+4,COLS);i++,j++){
            Color c = getType(j,i);
            if(c == color){
                if(contiB2>0){
                    isDenfense2 = 1;
                    break;
                }
                contiW2 ++;
            }
            else if(c == null){
                if(contiB2>0||contiW2>0||blankCount2==1)
                    break;
                blankCount2 = 1;
            }
            else{
                block2 = 1;
                if(contiW2>0)
                    break;
                contiB2 ++;
            }
        }

        if(contiW2+Math.min(x,y)<=3)
            block1 = 1;
        if(contiW1+Math.min(COLS-x,ROWS-y)<=3)
            block2 = 1;

        params2[0] = contiW2;
        params2[1] = contiB2;
        params2[2] = blankCount2;
        params2[3] = block2;
        params2[4] = isDenfense2;
        params1[0] = contiW1;
        params1[1] = contiB1;
        params1[2] = blankCount1;
        params1[3] = block1;
        params1[4] = isDenfense1;

        weight += countValue(params1,params2);

        if(color==Color.black)
            weight = -weight;
        return weight;
    }

    public int countValue(int[] params1,int[] params2){
        int factor = 2;
        int contiW1 = params1[0],contiW2 = params2[0],contiB1 = params1[1],contiB2 = params2[1];
        int blankCount1 = params1[2],blankCount2 = params2[2],block1 = params1[3],block2 = params2[3];
        int isDenfense1 = params1[4],isDenfense2 = params2[4];
        int blankCount = blankCount1 + blankCount2;
        int continueW = contiW1 + contiW2;
        int continueB = contiB1 + contiB2;
        boolean doubleBlock = false;
        int denfenseW = 0;
        int offenseW = 0;
        if(block1+block2==2)
            doubleBlock = true;
        else if(block1+block2==1)
            factor = 1;
        if(blankCount == 0) {
            if(continueB>=4)
                denfenseW += evaluateList.get("21111")*2;
            else if(continueB==3)
                denfenseW = evaluateList.get("2111")*2;
            else if(continueB==2)
                denfenseW += evaluateList.get("211")*2;
            else if(continueB==1)
                denfenseW += evaluateList.get("21")*2;

            if(continueW==1)
                offenseW += evaluateList.get("22")*factor;
            else if(continueW==2)
                offenseW += evaluateList.get("222")*factor;
            else if(continueW==3)
                offenseW += evaluateList.get("2222")*2;
            else if(continueW>=4)
                offenseW += evaluateList.get("22222")*2;
        }
        else if(blankCount == 1){
            if(blankCount1==1){
                if(contiW2==1)
                    offenseW += evaluateList.get("22")*factor;
                else if(contiW2==2)
                    offenseW += evaluateList.get("222")*factor;
                else if(contiW2==3)
                    offenseW += evaluateList.get("2222")*2;
                else if(contiW2>=4)
                    offenseW += evaluateList.get("22222")*2;

                if(contiW1==3)
                    offenseW += evaluateList.get("20222")*factor;
                else if(contiW1 == 2)
                    offenseW += evaluateList.get("2022")*factor;

                if(contiB2>=4)
                    denfenseW += evaluateList.get("21111")*2;
                else if(contiB2==3)
                    denfenseW += evaluateList.get("2111")*2;
                else if(contiB2==2)
                    denfenseW += evaluateList.get("211")*2;
                else if(contiB2==1)
                    denfenseW += evaluateList.get("21")*2;

                if(contiB1==2)
                    denfenseW += evaluateList.get("2011")*2;
            }
            if(blankCount2==1){
                if(contiW1==1)
                    offenseW += evaluateList.get("22")*factor;
                else if(contiW1==2)
                    offenseW += evaluateList.get("222")*factor;
                else if(contiW1==3)
                    offenseW += evaluateList.get("2222")*2;
                else if(contiW1>=4)
                    offenseW += evaluateList.get("22222")*2;

                if(contiW2==3)
                    offenseW += evaluateList.get("20222")*factor;
                else if(contiW2 == 2)
                    offenseW += evaluateList.get("2022")*factor;

                if(contiB1>=4)
                    denfenseW += evaluateList.get("21111")*2;
                else if(contiB1==3)
                    denfenseW += evaluateList.get("2111")*2;
                else if(contiB1==2)
                    denfenseW += evaluateList.get("211")*2;
                else if(contiB1==1)
                    denfenseW += evaluateList.get("21")*2;

                if(contiB2==2)
                    denfenseW += evaluateList.get("2011")*2;
            }
        }
        else{
            if(continueB==2)
                denfenseW += evaluateList.get("2011")*2;
            if(continueW==3)
                offenseW += evaluateList.get("20222")*factor;
            else if(continueW == 2)
                offenseW += evaluateList.get("2022")*factor;
        }
        if(doubleBlock==true)
            offenseW = 0;
        if(isDenfense1+isDenfense2>=1&&continueB!=4)
            denfenseW = 0;
        return (denfenseW+offenseW);
    }
}
