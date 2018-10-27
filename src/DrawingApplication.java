import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
//import java.io.*;
import java.util.Arrays;


public class DrawingApplication extends JFrame
{
    // GUI Component dimensions.
    private final int CANVAS_INITIAL_WIDTH = 800;
    private final int CANVAS_INITIAL_HEIGHT = 640;
    private final int CONTROL_PANEL_WIDTH = 200;
    private final int MESSAGE_AREA_HEIGHT = 100;

    // Freehand Parameters
    private final int MAX_FREEHAND_PIXELS = 1000;
    private Color[] freehandColour = new Color[MAX_FREEHAND_PIXELS];
    private int[][] fxy = new int[MAX_FREEHAND_PIXELS][3];
    private int freehandPixelsCount = 0;

    // Rectangle Parameters
    private final int MAX_RECT = 10;
    private Color[] rectColour = new Color[MAX_RECT];
    private int[][] rxy = new int[MAX_RECT][4];
    private int rectangleCount = 0;

    // Oval Parameters
    private final int MAX_OVAL = 10;
    private Color[] ovalColour = new Color[MAX_OVAL];
    private int[][] oxy = new int[MAX_OVAL][4];
    private int ovalCount = 0;

    // Line Parameters
    private final int MAX_LINE = 10;
    private Color[] lineColour = new Color[MAX_LINE];
    private int[][] lxy = new int[MAX_LINE][4];
    private int lineCount = 0;
    
    private Canvas canvas;
    
    private JPanel controlPanel;
    private JLabel coordinatesLabel;
    private JRadioButton lineRadioButton, ovalRadioButton, rectangleRadioButton, freehandRadioButton;
    private JSlider freehandSizeSlider;
    private JCheckBox fineCheckBox, coarseCheckBox;
    private JButton colourButton, clearButton, animateButton;

    private JColorChooser colourChooser;
    
    private JTextArea messageArea;
    
    private JMenuBar menuBar;
    
    private int freehandThickness = 50;

    private Color selectedColour = new Color(200, 40, 50);

    private int[] rubberBandCoords = new int[4];  // First two fields hold the start coordinates, last two hold current mouse coordinates
    private int shapeRubberBand = 0;  // 0 - Inactive; 1 - Rubber Band; 2 - Draw Rectangle


        // Drawing area class (inner class).
    class Canvas extends JPanel {

        // Called every time there is a change in the canvas contents.
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw(g);
        }
    } // end inner class Canvas

        
    
    /*****************************************************************
     * 
     * Constructor method starts here
     *    ... and goes on for quite a few lines of code 
     */
    public DrawingApplication()
    {
        setTitle("Drawing Application (da1)");
        setLayout(new BorderLayout());  // Layout manager for the frame.
        
        // Canvas
        canvas = new Canvas();
          canvas.setBorder(new TitledBorder(new EtchedBorder(), "Canvas"));
          canvas.setPreferredSize(new Dimension(CANVAS_INITIAL_WIDTH, CANVAS_INITIAL_HEIGHT));
          // next line changes the cursor's rendering whenever the mouse drifts onto the canvas
          canvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        add(canvas, BorderLayout.CENTER);
        
        // Menu bar
        menuBar = new JMenuBar();
          JMenu fileMenu = new JMenu("File");
            JMenuItem fileSaveMenuItem = new JMenuItem("Save");
            fileMenu.add(fileSaveMenuItem);
            JMenuItem fileLoadMenuItem = new JMenuItem("Load");
            fileMenu.add(fileLoadMenuItem);
            fileMenu.addSeparator();
            JMenuItem fileExitMenuItem = new JMenuItem("Exit");
            fileMenu.add(fileExitMenuItem);
          menuBar.add(fileMenu);
          JMenu helpMenu = new JMenu("Help");
            JMenuItem helpAboutMenuItem = new JMenuItem("About");
            helpMenu.add(helpAboutMenuItem);
          menuBar.add(helpMenu);
        add(menuBar, BorderLayout.PAGE_START);
        
        // Control Panel
        controlPanel = new JPanel();
          controlPanel.setBorder(new TitledBorder(new EtchedBorder(), "Control Panel"));
          controlPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH, CANVAS_INITIAL_HEIGHT));
          // the following two lines put the control panel in a scroll pane (nicer?).      
          JScrollPane controlPanelScrollPane = new JScrollPane(controlPanel);
          controlPanelScrollPane.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH + 30, CANVAS_INITIAL_HEIGHT));
        add(controlPanelScrollPane, BorderLayout.LINE_START);        

        
        // Control Panel contents are specified in the next section eg: 
        //    mouse coords panel; 
        //    shape tools panel; 
        //    trace-slider panel; 
        //    grid panel; 
        //    colour choice panel; 
        //    "clear" n "animate" buttons
        
        // Mouse Coordinates panel
        JPanel coordinatesPanel = new JPanel();
          coordinatesPanel.setBorder(new TitledBorder(new EtchedBorder(), "Drawing Position"));
          coordinatesPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 60));
          coordinatesLabel = new JLabel();
          coordinatesPanel.add(coordinatesLabel);
          coordinatesLabel.setText("some text");
        controlPanel.add(coordinatesPanel);
        
        // Drawing tools panel
        JPanel drawingToolsPanel = new JPanel();
        drawingToolsPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 140));
        drawingToolsPanel.setLayout(new GridLayout(0, 1));
        drawingToolsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Drawing Tools"));
        controlPanel.add(drawingToolsPanel);

        // Drawing Tools Panel
        ButtonGroup drawingToolsButtonGroup = new ButtonGroup();

        freehandRadioButton = new JRadioButton();
        freehandRadioButton.setText("Freehand");
        drawingToolsPanel.add(freehandRadioButton);

        rectangleRadioButton = new JRadioButton();
        rectangleRadioButton.setText("Rectangle");
        drawingToolsPanel.add(rectangleRadioButton);

        ovalRadioButton = new JRadioButton();
        ovalRadioButton.setText("Oval");
        drawingToolsPanel.add(ovalRadioButton);

        lineRadioButton = new JRadioButton();
        lineRadioButton.setText("Line");
        drawingToolsPanel.add(lineRadioButton);

        drawingToolsButtonGroup.add(freehandRadioButton);
        drawingToolsButtonGroup.add(rectangleRadioButton);
        drawingToolsButtonGroup.add(ovalRadioButton);
        drawingToolsButtonGroup.add(lineRadioButton);

        // Freehand trace size slider
        JPanel freehandSliderPanel = new JPanel();
        freehandSliderPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 90));
        drawingToolsPanel.setLayout(new GridLayout(0, 1));
        freehandSliderPanel.setBorder(new TitledBorder(new EtchedBorder(), "Freehand Size"));
        controlPanel.add(freehandSliderPanel);
        
        freehandSizeSlider = new JSlider();
        freehandSizeSlider.setMinimum(1);
        freehandSizeSlider.setMaximum(100);
        freehandSizeSlider.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 30, 50));
        
        freehandSliderPanel.add(freehandSizeSlider);

        // Grid Panel
        JPanel gridPanel = new JPanel();
          gridPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 80));
          gridPanel.setLayout(new GridLayout(0, 1));
          gridPanel.setBorder(new TitledBorder(new EtchedBorder(), "Grid"));
        controlPanel.add(gridPanel);
        
        fineCheckBox = new JCheckBox();
        fineCheckBox.setText("Fine");
        gridPanel.add(fineCheckBox);
        fineCheckBox.addActionListener(new CheckBoxActionListener());
        
        coarseCheckBox = new JCheckBox();
        coarseCheckBox.setText("Coarse");
        gridPanel.add(coarseCheckBox);
        coarseCheckBox.addActionListener(new CheckBoxActionListener());
        
        // Colour Panel
        JPanel colourPanel = new JPanel();
          colourPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 90));
          colourPanel.setBorder(new TitledBorder(new EtchedBorder(), "Colour"));
          colourButton = new JButton();
          colourButton.setPreferredSize(new Dimension(50, 50));
          colourButton.setBackground(selectedColour);
          colourPanel.add(colourButton);
        controlPanel.add(colourPanel);

        // Colour Chooser
        colourChooser = new JColorChooser(selectedColour);

        // Clear button
        clearButton = new JButton("Clear Canvas");
          clearButton.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 50));
        controlPanel.add(clearButton);

        // Animate button 
        animateButton = new JButton("Animate");
          animateButton.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 50));
        controlPanel.add(animateButton);
        
        // that completes the control panel section

        
        // Message area
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setBackground(canvas.getBackground());
        JScrollPane textAreaScrollPane = new JScrollPane(messageArea);
        textAreaScrollPane.setBorder(new TitledBorder(new EtchedBorder(), "Message Area"));
        textAreaScrollPane.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH + CANVAS_INITIAL_WIDTH, MESSAGE_AREA_HEIGHT));
        add(textAreaScrollPane, BorderLayout.PAGE_END);

        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        pack();
        setVisible(true);
        
        canvas.addMouseMotionListener(new CanvasMouseMotionListener());
        canvas.addMouseListener(new CanvasMouseListener());
        freehandSizeSlider.addChangeListener(new SliderChangeListener());
        colourButton.addActionListener(new ColorButtonActionListener());
        clearButton.addActionListener(new ClearButtonActionListener());
        
    }  // end of the DrawingApplication constructor method
    
    // Called by the canvas' paintComponent method
    void draw(Graphics g)
    {
        // Code for drawing Gridlines
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();
        if(fineCheckBox.isSelected()){
            g.setColor(new Color(0.8F, 0.8F, 0.8F));
            for(int i=10; i<canvasWidth+10; i=i+10){
                g.drawLine(i, 10, i, canvasHeight-10);
            }
            for(int i=10; i<canvasHeight+10; i=i+10){
                g.drawLine(10, i, canvasWidth-10, i);
            }
        }
        if(coarseCheckBox.isSelected()){
            g.setColor(new Color(0.6F, 0.6F, 0.6F));
            for(int i=50; i<canvasWidth+50; i=i+50){
                g.drawLine(i, 10, i, canvasHeight-10);
            }
            for(int i=50; i<canvasHeight+50; i=i+50){
                g.drawLine(10, i, canvasWidth-10, i);
            }
        }

        // Shape Drawing
        for (int i = 0; i < freehandPixelsCount; i++) {
            g.setColor(freehandColour[i]);
            g.fillRect(fxy[i][0], fxy[i][1], fxy[i][2], fxy[i][2]);
        }
        for (int i = 0; i < rectangleCount; i++){
            int width = rxy[i][2] - rxy[i][0];
            int height = rxy[i][3] - rxy[i][1];
            g.setColor(rectColour[i]);
            g.fillRect(rxy[i][0], rxy[i][1], width, height);
        }
        for(int i = 0; i < ovalCount; i++){
            int width = oxy[i][2] - oxy[i][0];
            int height = oxy[i][3] - oxy[i][1];
            g.setColor(ovalColour[i]);
            g.fillOval(oxy[i][0], oxy[i][1], width, height);
        }
        for(int i = 0; i < lineCount; i++){
            g.setColor(lineColour[i]);
            g.drawLine(lxy[i][0], lxy[i][1], lxy[i][2], lxy[i][3]);
        }
        canvas.repaint();
        int width = rubberBandCoords[2] - rubberBandCoords[0];
        int height = rubberBandCoords[3] - rubberBandCoords[1];
        if(shapeRubberBand == 1 && rectangleRadioButton.isSelected()){
            g.drawRect(rubberBandCoords[0], rubberBandCoords[1], width, height);
        }
        if(shapeRubberBand == 1 && ovalRadioButton.isSelected()){
            g.drawOval(rubberBandCoords[0], rubberBandCoords[1], width, height);
        }
        if(shapeRubberBand == 1 && lineRadioButton.isSelected()){
            g.drawLine(rubberBandCoords[0], rubberBandCoords[1], rubberBandCoords[2], rubberBandCoords[3]);
        }



    } // end draw method

    public void freehandDraw(int a, int b)
    {
        if(freehandPixelsCount <= 998){
            fxy[freehandPixelsCount][0] = a;
            fxy[freehandPixelsCount][1] = b;
            fxy[freehandPixelsCount][2] = freehandThickness;
            freehandColour[freehandPixelsCount] = selectedColour;
            canvas.repaint();
            freehandPixelsCount++;
            messageArea.append("You Have " + (1000-freehandPixelsCount) + " Pixels of ink left \n");
        }
        else{
            messageArea.append("You are out of ink \n");
        }
    }

    
    public static void main(String args[])
    {
        DrawingApplication drawingApplicationInstance = new DrawingApplication();
    } // end main method
    
    class CanvasMouseListener implements MouseListener
    {
        public void mousePressed(MouseEvent event){
            rubberBandCoords[0] = event.getX();
            rubberBandCoords[1] = event.getY();
        }
        
        public void mouseReleased(MouseEvent event){
            shapeRubberBand = 2;
            if(rectangleRadioButton.isSelected()){
                if(rectangleCount<10) {
                    rxy[rectangleCount][0] = rubberBandCoords[0];
                    rxy[rectangleCount][1] = rubberBandCoords[1];
                    rxy[rectangleCount][2] = event.getX();
                    rxy[rectangleCount][3] = event.getY();
                    rectColour[rectangleCount] = selectedColour;
                    rectangleCount++;
                    messageArea.append("You Have " + (10 - rectangleCount) + " Rectangles left \n");
                    canvas.repaint();
                }
                else messageArea.append("Maximum number of rectangles reached \n");
            }
            if(ovalRadioButton.isSelected()) {
                if (ovalCount < 10) {
                    oxy[ovalCount][0] = rubberBandCoords[0];
                    oxy[ovalCount][1] = rubberBandCoords[1];
                    oxy[ovalCount][2] = event.getX();
                    oxy[ovalCount][3] = event.getY();
                    ovalColour[ovalCount] = selectedColour;
                    ovalCount++;
                    messageArea.append("You have " + (10 - ovalCount) + " Ovals left \n");
                    canvas.repaint();
                }
                else messageArea.append("Maximum number of ovals reached \n");
            }
            if(lineRadioButton.isSelected()){
                if(lineCount < 10){
                    lxy[lineCount][0] = rubberBandCoords[0];
                    lxy[lineCount][1] = rubberBandCoords[1];
                    lxy[lineCount][2] = event.getX();
                    lxy[lineCount][3] = event.getY();
                    lineColour[lineCount] = selectedColour;
                    lineCount++;
                    messageArea.append("You have " + (10-lineCount) + " Lines left \n");
                    canvas.repaint();
                }
                else messageArea.append("Maximum number of lines reached \n");
            }
        }
        
        public void mouseClicked(MouseEvent event){
            if(freehandRadioButton.isSelected()) {
                freehandDraw(event.getX(), event.getY());
            }
        }
        
        public void mouseEntered(MouseEvent event){ }
        
        public void mouseExited(MouseEvent event){ }
    }
    
    class CanvasMouseMotionListener implements MouseMotionListener
    {
        public void mouseMoved(MouseEvent event)
        {
            coordinatesLabel.setText(event.getX() + " " + event.getY());
        }  // end of mouseMoved method
        
        public void mouseDragged(MouseEvent event)
        {
            coordinatesLabel.setText(event.getX() + " " + event.getY());
            if(freehandRadioButton.isSelected()) {
                freehandDraw(event.getX(), event.getY());
            }
            else{
                shapeRubberBand = 1;
                rubberBandCoords[2] = event.getX();
                rubberBandCoords[3] = event.getY();
                // rectangleDraw(drawXCoordinate, drawYCoordinate, event.getX(), event.getY());
            }
            canvas.repaint();
        }  // end of mouseDragged
    }  // end of CanvasMouseMotionListener class
    
    
    class CheckBoxActionListener implements ActionListener
    {
        public void actionPerformed (ActionEvent event)
        {
            canvas.repaint();
        }  // end actionPerformed method
    }  // end CheckBoxActionListener class


    class SliderChangeListener implements ChangeListener
    {
        public void stateChanged(ChangeEvent event){
            freehandThickness = freehandSizeSlider.getValue();
        }

    }

    class ColorButtonActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event){
            selectedColour = colourChooser.showDialog(null, "Choose new drawing colour", selectedColour);
            colourButton.setBackground(selectedColour);
        }

    }

    class ClearButtonActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event){
            for(int i=0; i>freehandPixelsCount; i++){
                fxy[i][0] = 0;
                fxy[i][1] = 0;
                fxy[i][2] = 0;
            }
            for(int i=0; i>rectangleCount; i++){
                rxy[i][0] = 0;
                rxy[i][1] = 0;
                rxy[i][2] = 0;
                rxy[i][3] = 0;
            }
            for(int i=0; i>ovalCount; i++){
                oxy[i][0] = 0;
                oxy[i][1] = 0;
                oxy[i][2] = 0;
                oxy[i][3] = 0;
            }
            freehandPixelsCount = 0;
            rectangleCount = 0;
            ovalCount = 0;
            lineCount = 0;
            Arrays.fill(freehandColour, null);
            Arrays.fill(rectColour, null);
            Arrays.fill(ovalColour, null);
            Arrays.fill(lineColour, null);
            canvas.repaint();
            messageArea.setText("");
        }
    }
} // end of DrawingApplication class
