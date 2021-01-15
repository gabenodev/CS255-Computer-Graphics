/**
 * CS-255 Computer Graphics Assignment
 * @author Gabriel Petcu
 * @version 1.0.0
 * Every word wrote in this code it was made by me
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;

import static java.lang.Integer.min;
import static java.lang.StrictMath.max;

// OK this is not best practice - maybe you'd like to create
// a volume data class?
// I won't give extra marks for that though.

public class Example extends Application {
    short cthead[][][]; //store the 3D volume data set
    short min, max; //min/max value in the 3D volume data set

    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException {
        stage.setTitle("CThead Viewer");


        ReadData();


        int width = 256;
        int height = 256;
        Label HTOP = new Label("Histogram top");
        Label HSIDE = new Label("Histogram side");
        Label HFRONT = new Label("Histogram front");
         //We need 3 things to see an image
        //1. We create an image we can write to
        WritableImage medical_image = new WritableImage(width, height);
        WritableImage medical_image2 = new WritableImage(255,113);
        WritableImage medical_image3 = new WritableImage(255,113);
       // WritableImage medical_image4 = medical_image;
        WritableImage medical_image4 = new WritableImage(width,height);
        //2. We create a view of that image
        ImageView imageView = new ImageView(medical_image);
        ImageView imageView2 = new ImageView(medical_image2);
        ImageView imageView3 = new ImageView(medical_image3);
        ImageView imageView4 = new ImageView(medical_image4);

        //sliders to step through the slices (z and y directions) (remember 113 slices in z direction 0-112)
        Slider zslider = new Slider(0, 112, 0);
        Slider yslider = new Slider(0, 255, 0);
        Slider xslider = new Slider(0,255,0);

        Slider HTOPslider = new Slider(0,112,0);
        Slider HSIDEslider = new Slider(0,255,0);
        Slider HFRONTslider = new Slider(0,255,0);

        Slider resizeNearslider = new Slider(0,513,0);

        Button MIPT_button=new Button("MIPT");
        Button MIPF_button=new Button("MIPF");
        Button MIPS_button=new Button("MIPS");


       // Maximum intensity buttons

        MIPT_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                viewing(medical_image,70,1, true);
            }
        });
        MIPF_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                viewing(medical_image2,70,2, true);
            }
        });
        MIPS_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                viewing(medical_image3,70,3, true);
            }
        });


         //All sliders including top,front,side and resize functions.


        resizeNearslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        resizeNear(medical_image4, (int)resizeNearslider.getValue(),96);
                    }
                });
        HTOPslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                         Histograms(medical_image, newValue.intValue(),1);
                    }
                });
        HFRONTslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        Histograms(medical_image2, newValue.intValue(),2);
                    }
                });
        HSIDEslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        Histograms(medical_image3, newValue.intValue(),3);
                    }
                });

        zslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {

                            viewing(medical_image, newValue.intValue(),1,false);
                    }
                });

        yslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        viewing(medical_image2, newValue.intValue(),2,false);
                    }
                });
        xslider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    public void changed(ObservableValue <? extends Number >
                                                observable, Number oldValue, Number newValue)
                    {
                        viewing(medical_image3, newValue.intValue(),3,false);
                    }
                });

        FlowPane root = new FlowPane();
        root.setVgap(8);
        root.setHgap(4);
//https://examples.javacodegeeks.com/desktop-java/javafx/scene/image-scene/javafx-image-example/

        //3. (referring to the 3 things we need to display an image)
        //we need to add it to the flow pane
        root.getChildren().addAll(imageView,zslider,MIPT_button,HTOP,HTOPslider,imageView2,yslider,MIPF_button,HFRONT,HFRONTslider,imageView3,xslider,MIPS_button,HSIDE,HSIDEslider,imageView4,resizeNearslider);


        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    //Function to read in the cthead data set
    public void ReadData() throws IOException {
        //File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
        File file = new File("CThead.raw");
        //Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find if there is an equivalent in Java)
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i , j, k; //loop through the 3D data set

        min=Short.MAX_VALUE; max=Short.MIN_VALUE; //set to extreme values
        short read; //value read in
        int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

        cthead = new short[113][256][256]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k=0; k<113; k++) {
            for (j=0; j<256; j++) {
                for (i=0; i<256; i++) {
                    //because the Endianess is wrong, it needs to be read byte at a time and swapped
                    b1=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    b2=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    read=(short)((b2<<8) | b1); //and swizzle the bytes around
                    if (read<min) min=read; //update the minimum
                    if (read>max) max=read; //update the maximum
                    cthead[k][j][i]=read; //put the short into memory (in C++ you can replace all this code with one fread)
                }
            }
        }
        System.out.println(min+" "+max);
        //diagnostic - for CThead this should be -1117, 2248
        //(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
        //therefore histogram equalization would be a good thing
        //maybe put your histogram equalization code here to set up the mapping array
       // int histogram[] = new int[max-min+1];
        //int index = cthead[k][j][i]-min;
        //for (int p = 0 ; p < max-min+1 ; p++)
          //  histogram[p] = 0;
        //int minimum = -1117;
       // min = min(cthead[k][j][i],min);
        //int maximum = 2248;
       // max = Short.max(cthead[k][j][i],max);

            //histogram[index]++;



    }


    /*
       This function shows how to carry out an operation on an image.
       It obtains the dimensions of the image, and then loops through
       the image carrying out the copying of a slice of data into the
       image.
   */

    /**
     * This method works with the image and displays all 3 views for the image(SIDE,TOP,FRONT) and Maximum Intensity Projection.
     * @param image parsing the image
     * @param s represents the slice
     * @param view represents all 3 views(top,side,front)
     * @param pressed represents if maximum intensity projection is either pressed or not
     */

    public void viewing(WritableImage image, int s, int view , boolean pressed) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight(), i, j, c, k;
        PixelWriter image_writer = image.getPixelWriter();

        float col;
        short datum;

        //Shows how to loop through each pixel and colour
        //Try to always use j for loops in y, and i for loops in x
        //as this makes the code more readable
        // THIS IS TOP VIEWING
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                //at this point (i,j) is a single pixel in the image
                //here you would need to do something to (i,j) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your j,i could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also l eaves you something to do for the assignment
                if(view == 1 && pressed ) {
                    int maximum = 0;
                    for(k = 0; k < 113; k++) {
                        maximum = max(cthead[k][j][i], maximum);
                    }
                    col = (((float) maximum - (float) min) / ((float) (max - min)));
                        image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                }// if bracket for view = 1
                 else if( view == 1 && !pressed)
                {
                    datum = cthead[s][j][i];
                    col = (((float) datum - (float) min) / ((float) (max - min)));
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                }
                if (view == 2 && pressed )
                {
                    int maximum = 0;
                    for(k = 0; k < 256 ; k++)
                    {
                        maximum = max(cthead[j][k][i],maximum);
                    }
                    col = (((float) maximum - (float) min) / ((float) (max - min)));
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                } else if ( view == 2 && !pressed )
                {

                   datum = cthead[j][s][i];
                    col = (((float) datum - (float) min) / ((float) (max - min)));
                   image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                } // if bracket for view = 2

                if (view == 3 && pressed)
                {
                    int maximum = 0;
                    for(k=0 ; k<256;k++)
                    {
                        maximum = max(cthead[j][i][k] , maximum);
                    }
                    col = (((float) maximum - (float) min) / ((float) (max - min)));
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                } else if ( view == 3 && !pressed)
                {
                    datum = cthead[j][i][s];
                    col = (((float) datum - (float) min) / ((float) (max - min)));
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                } // If bracket for view = 3.
            } // column loop
        } // row loop
    }

    /**
     * This method displays histograms for all 3 views(TOP,SIDE,FRONT).
     * @param image represents the image we are working on.
     * @param s represents the slice for each of the 3 views.
     * @param view represents the all 3 viewing sides(TOP,FRONT,SIDE).
     */
    public void Histograms(WritableImage image, int s, int view)
{   int i ,j, k;
    PixelWriter image_writer = image.getPixelWriter();
    int w=(int) image.getWidth(), h=(int) image.getHeight();
    short datum;
    int[] histogram = new int[max-min+1];
    int[] t = new int[max-min+1];
    int index;
    float Size = 7405568;
    float mapping[]=new float[max-min+1];
    float col;
    for (int p = 0 ; p < max-min+1 ; p++) {
        histogram[p] = 0;
    }
    if (view == 1) {
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                for (k = 0; k < 113; k++) {
                    index = cthead[k][j][i] - min;
                    histogram[index]++;
                }

            }
        }
    } // if view == 1
    if (view == 2) {
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                for (k = 0; k < 256; k++) {
                    index = cthead[j][k][i] - min;
                    histogram[index]++;
                }

            }
        }
    } // if view == 2
    if (view == 3) {
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                for (k = 0; k < 256; k++) {
                    index = cthead[j][i][k] - min;
                    histogram[index]++;
                }

            }
        }
    } // if view == 3
        for (int n = 0; n < max - min + 1; n++) {
            if (n == 0) {
                t[0] = histogram[0];

            } else {
                t[n] = t[n - 1] + histogram[n];
                //System.out.println(t[n]);
            }
            // Mapping the color.
            mapping[n] = (t[n] / Size);
            // System.out.println(mapping[n]);
        }
         if(view == 1 ) {
             for (j = 0; j < h; j++) {
                 for (i = 0; i < w; i++) {
                     datum = cthead[s][j][i];
                     col = mapping[datum - min];
                     image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

                 }
             }
         } // if view == 1
    if (view == 2)
    {
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                datum = cthead[j][s][i];
                col = mapping[datum - min];
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

            }
        }
    } // if view == 2

    if (view == 3)
    {
        for (j = 0; j < h; j++) {
            for (i = 0; i < w; i++) {
                datum = cthead[j][i][s];
                col = mapping[datum - min];
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

            }
        }
    } // if view == 3

}

    /**
     * This method resizes an image from the TOP VIEWING.
     * @param image represents the image we are working on
     * @param size represents the maximum size that we want the image to go on
     * @param s represents the actual slice that we are working with.
     * @return the new image that we created.
     */
    public WritableImage resizeNear(WritableImage image, float size,int s) {
        float x, y;
        short datum;
        float col;

        float xa = (int) image.getWidth();
        float ya = (int) image.getHeight();


        WritableImage preImage = new WritableImage((int)xa,(int)ya);
        PixelWriter image_writer = image.getPixelWriter();

        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                    x = i * (float) (ya / size);
                    y = j * (float) (xa / size);
                    x = Math.round(x);
                    y = Math.round(y);

                datum = cthead[s][(int)y][(int)x];
                col = (((float) datum - (float) min) / ((float) (max - min)));
                image_writer.setColor(i, j, Color.color(col, col, col, 1.0));


            }
        }
      return preImage;

    }

    public static void main(String[] args) {
        launch();
    }

}