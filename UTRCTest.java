/*
    The task is to morph a given sphere into a cube by moving the vertices of the sphere, while ensuring that the vertices travel the shortest distance possible.

    To ensure minimum travel distance for the vertices, the bounds of the sphere vertices is computed along the x, y and z axes to ensure that when the vertices are moved no vertex is moved beyond the axes bounds of the original sphere.

    For each vertex, identify which face the vertex belongs to by finding the largest absolute value of the x, y and z coordinates. Move this value to the closer of the max or min bounds on the respective axis.

    Solve for the other two values in the vertex using the formula here: http://alexcpeterson.com/2010/04/23/sphere-to-cube-mapping/

    Place input file "Test.obj" in the same folder as the java program
    
    Run program

    Output file "output.obj" will be generated in the same folder

*/

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
/**
 *
 * @author Ade
 */
public class UTRCTest 
{
    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException
    {       
        try
        {
            //create input file and initialise geometry object
            //read input from file on disk and parse file to obtain vertices and faces
            //add vertices and faces to geometry object
            File sphereObj = new File("Test.obj");
            Geometry sphere = new Geometry("Input sphere");
            Scanner input = new Scanner(sphereObj);
            String[] line;
            while(input.hasNextLine())
            {
                line = input.nextLine().trim().split("\\s+");
                
                if(line.length>=2 && line[0].equals("s") && line[0].equals("0"))    //this assumes that the mesh is polygonal hence smoothing groups is turned off
                {
                    sphere.addSmoothingGroupInfo(line[0]+" "+line[1]);
                }
                
                if(line.length>=4 && line[0].equals("v"))
                {
                    sphere.addVertex(new Vertex(Double.parseDouble(line[1]), Double.parseDouble(line[2]), Double.parseDouble(line[3])));
                }

                if(line.length>=4 && line[0].equals("f"))
                {
                    sphere.addFace(new Face(Integer.parseInt(line[1]), Integer.parseInt(line[2]), Integer.parseInt(line[3])));
                }
            }
            
            //move vertices, create output file on disk and write to file
            System.out.println(sphere); //print vertex and face count
            sphere.moveVertices();
            sphere.writeToFile("output");
        }
        
        catch(FileNotFoundException fn)
        {
            System.out.println("No input file found. Please place the input obj file in the running directory with the appropriate name.");            
        }
    }
}

//this class describes the input and output 3D geometry 
//It contains a list of vertex objects, list of face objects and a label (name) 
class Geometry
{
    List<Vertex> vertices;
    List<Face> faces;
    String label;
    String smoothingGroupInfo;
    
    //initialise geometry
    public Geometry(String l)
    {
        label = l;
        vertices = new ArrayList<>();
        faces = new ArrayList<>();
        smoothingGroupInfo = "s 0"; //default smoothing group information for polygonal mesh with smoothing group turned off
    }
    
    //update smoothing group information if present in original file
    public void addSmoothingGroupInfo(String info)
    {
        smoothingGroupInfo = info;
    }
    
    //return smoothing group information
    public String getSmoothingGroupInfo()
    {
        return smoothingGroupInfo;
    }
    
    //add vertex object
    public void addVertex(Vertex v)
    {
        vertices.add(v);
    }
    
    //add face object
    public void addFace(Face f)
    {
        faces.add(f);
    }
    
    //create and write to .obj output file on disk
    public void writeToFile(String fileName) throws FileNotFoundException
    {
        try
        {
            PrintWriter output = new PrintWriter(fileName+".obj");
            output.println(smoothingGroupInfo);
            for(Vertex v : vertices)
                output.println(v);
            for(Face f : faces)
                output.println(f);
            output.close();
            System.out.println("Output file written to "+fileName+".obj");
        }
        
        catch(FileNotFoundException fn)
        {
            System.out.println("Error writing to output file!!");
        }
    }
    
    //get string representation of geometry
    @Override
    public String toString()
    {
        return label+", Vertex count: "+vertices.size()+", Face count: "+faces.size();
    }
    
    //move vertices the shortest length possible
    public void moveVertices()
    {
        Geometry polyhedron = this;
        polyhedron.label = "Output Polyhedron";
        double xMaxBounds, yMaxBounds, zMaxBounds, xMinBounds, yMinBounds, zMinBounds;	//the min and max bounds of the mesh along the x, y and z axes
        double currentX, currentY, currentZ, closestPoint;  //variables used to store values of the current vertex
        final double const1 = Math.sqrt(0.5);   //a constant that is used in the formula for mapping a sphere to a cube
        final double const2 = 12;   //a constant that is used in the formula for mapping a sphere to a cube
	double exp1, exp2, exp3, exp4;  //variables used to store repeating expressions
        double cubeX, cubeY, cubeZ; //variables to store computed cube vertices
        
        //initialise mesh bounds with max and min double values
	xMaxBounds = yMaxBounds = zMaxBounds = Double.MIN_VALUE;
	xMinBounds = yMinBounds = zMinBounds = Double.MAX_VALUE;

	//compute the maximum and minimum mesh bounds by getting the max and min values along the x, y and z axes
	//Note: The absolute difference between the max and min bounds on any axis is the diameter of the sphere along that axis
	//and the value is the same along the three axes for a perfectly-rounded shpere
        for(Vertex v : vertices)
        {
            xMaxBounds = v.x > xMaxBounds ? v.x : xMaxBounds;
            xMinBounds = v.x < xMinBounds ? v.x : xMinBounds;
            yMaxBounds = v.y > yMaxBounds ? v.y : yMaxBounds;
            yMinBounds = v.y < yMinBounds ? v.y : yMinBounds;
            zMaxBounds = v.z > zMaxBounds ? v.z : zMaxBounds;
            zMinBounds = v.z < zMinBounds ? v.z : zMinBounds;
        }

        //move each vertex based on the sphere-to-cube mapping formula
        for(Vertex v : vertices)
	{
            currentX = v.x; currentY = v.y; currentZ = v.z; //store current vertex values
            closestPoint = maxVal(v.x, v.y, v.z);   //get absolute largest of the three vertex values

            if (areEqual(closestPoint, v.x))    //true if x value is the largest of x, y and z, meaning the vertex should be moved to the x plane i.e. right or left face
            {
                //move x to the closer of the right or left face
                cubeX = isCloserToMax(currentX, xMaxBounds, xMinBounds) ? xMaxBounds : xMinBounds;
                //solve for y and z
                exp1 = currentY * currentY * 2.0;
                exp2 = currentZ * currentZ * 2.0;
                exp3 = -exp1 + exp2 -3;
                exp4 = -Math.sqrt((exp3 * exp3) - const2 * exp1);                
                cubeY = currentY == 0.0 || currentY == -0.0 ? 0.0 : Math.sqrt(exp4 + exp1 - exp2 + 3.0) * const1;
                cubeY = currentY < 0 ? -cubeY : cubeY;
                cubeZ = currentZ == 0.0 || currentZ == -0.0 ? 0.0 : Math.sqrt(exp4 - exp1 + exp2 + 3.0) * const1;                
                cubeZ = currentZ < 0 ? -cubeZ : cubeZ;
            }
            else if (areEqual(closestPoint, v.y))   //true if y value is the largest of x, y and z, meaning the vertex should be moved to the y plane i.e. top or bottom face
            {
                //move y to the closer of the top or bottom face
                cubeY = isCloserToMax(currentY, yMaxBounds, yMinBounds) ? yMaxBounds : yMinBounds;
                //solve for x and z
                exp1 = currentX * currentX * 2.0;
                exp2 = currentZ * currentZ * 2.0;
                exp3 = -exp1 + exp2 -3;
                exp4 = -Math.sqrt((exp3 * exp3) - const2 * exp1);
                cubeX = currentX == 0.0 || currentX == -0.0 ? 0.0 : Math.sqrt(exp4 + exp1 - exp2 + 3.0) * const1;
                cubeX = currentX < 0 ? -cubeX : cubeX;                
                cubeZ = currentZ == 0.0 || currentZ == -0.0 ? 0.0 : Math.sqrt(exp4 - exp1 + exp2 + 3.0) * const1;                
                cubeZ = currentZ < 0 ? -cubeZ : cubeZ;
            }
            else if (areEqual(closestPoint, v.z))   //true if z value is the largest of x, y and z, meaning the vertex should be moved to the z plane i.e. front or back face
            {
                //move z to the closer of the front or back face
                cubeZ = isCloserToMax(currentZ, zMaxBounds, zMinBounds) ? zMaxBounds : zMinBounds;
                //solve for x and y
                exp1 = currentX * currentX * 2.0;
                exp2 = currentY * currentY * 2.0;
                exp3 = -exp1 + exp2 -3;
                exp4 = -Math.sqrt((exp3 * exp3) - const2 * exp1);
                cubeX = currentX == 0.0 || currentX == -0.0 ? 0.0 : Math.sqrt(exp4 + exp1 - exp2 + 3.0) * const1;
                cubeX = currentX < 0 ? -cubeX : cubeX;                
                cubeY = currentY == 0.0 || currentY == -0.0 ? 0.0 : Math.sqrt(exp4 - exp1 + exp2 + 3.0) * const1;                
                cubeY = currentY < 0 ? -cubeY : cubeY;
            }
            else    //in the unlikely case that none of the three previous conditions are true, move the vertex to the nearest cube corner
            {
                cubeX = isCloserToMax(currentX, xMaxBounds, xMinBounds) ? xMaxBounds : xMinBounds;
                cubeY = isCloserToMax(currentY, yMaxBounds, yMinBounds) ? yMaxBounds : yMinBounds;
                cubeZ = isCloserToMax(currentZ, zMaxBounds, zMinBounds) ? zMaxBounds : zMinBounds;
            }
            v.moveVertex(cubeX, cubeY, cubeZ);
	}
    }
    
    //return true if the distance between curr and max is smaller than the distance between curr and min
    Boolean isCloserToMax(double curr, double max, double min)	
    {
        return Math.abs(curr - max) <= Math.abs(curr - min);
    }
    
    //return the largest of three double numbers
    double maxVal(double x, double y, double z)	
    {
        return Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
    }

    //checks whether two double are equal. It is a more reliable alternative to using the "==" operator on double numbers
    Boolean areEqual(double x, double y)	
    {
        final double epsilon = 0.00000000000000001;	//this constant is the margin for imprecision used to check whether two double are equal
        return Math.abs(x) - Math.abs(y) <= epsilon;
    }
        
    //this method moves vertices to the nearest of the 8 vertices of a cube. 
    //while this produces a perfect cube, it does not solve the required task because the travel distance of the vertices is more than necessary
    public void morphToCube()
    {
        Geometry cube = this;
        cube.label = "Output Cube";
        double morphConstant = (double) Math.sqrt(Math.PI / 6);	// a constant used to obtain the bounds of a cube with equivalent surface area of the input sphere. It is obtained by equating the surface area formulae for a cube and sphere and solving for the length of a cube side
        double xMaxBounds, yMaxBounds, zMaxBounds, xMinBounds, yMinBounds, zMinBounds;	//the min and max bounds of the mesh along the x, y and z axes
        double currentX, currentY, currentZ;	//variables used to store values of the current vertex

	//initialise mesh bounds with max and min double values
	xMaxBounds = yMaxBounds = zMaxBounds = Double.MIN_VALUE;
	xMinBounds = yMinBounds = zMinBounds = Double.MAX_VALUE;

	//compute actual mesh bounds by getting the max and min values along the x, y and z axes
	//Note: The absolute difference between the max and min bounds on any axis is the diameter of the sphere along that axis
	//and the value is the same along the three axes for a perfectly-rounded shpere
        for(Vertex v : vertices)
        {
            xMaxBounds = v.x > xMaxBounds ? v.x : xMaxBounds;
            xMinBounds = v.x < xMinBounds ? v.x : xMinBounds;
            yMaxBounds = v.y > yMaxBounds ? v.y : yMaxBounds;
            yMinBounds = v.y < yMinBounds ? v.y : yMinBounds;
            zMaxBounds = v.z > zMaxBounds ? v.z : zMaxBounds;
            zMinBounds = v.z < zMinBounds ? v.z : zMinBounds;
        }
        
	//multiply the bounds of the sphere by the morphconstant to get the bounds for the cube of equivalent surface area
        xMaxBounds *= morphConstant;
        xMinBounds *= morphConstant;
        yMaxBounds *= morphConstant;
        yMinBounds *= morphConstant;
        zMaxBounds *= morphConstant;
        zMinBounds *= morphConstant;

        //for each vertex, move the x, y and z coordinates to the closer of the max bound and min bound along the x, y and z axes respectively
	//then update the vertex in the mesh to see how the morphing occurs in real-time
        for(Vertex v : vertices)
        {
            currentX = isCloserToMax(v.x, xMaxBounds, xMinBounds) ? xMaxBounds : xMinBounds;
            currentY = isCloserToMax(v.y, yMaxBounds, yMinBounds) ? yMaxBounds : yMinBounds;
            currentZ = isCloserToMax(v.z, zMaxBounds, zMinBounds) ? zMaxBounds : zMinBounds;
            v.moveVertex(currentX, currentY, currentZ);
        }
    }
}

//this class describes a vertex as three double type numbers: x, y, z
class Vertex
{
    public double x, y, z;
    
    //initialise vertex
    public Vertex(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;        
    }
    
    //move vertex to new position
    public void moveVertex(double newX, double newY, double newZ)
    {
        x = newX;
        y = newY;
        z = newZ;
    }    
    
    //return a string representation of the vertex
    @Override
    public String toString()
    {
        return "v "+x+" "+y+" "+z;
    }
}
//this class describes a face as three int type numbers: v1, v2, v3 which represent the 1-based indices of the geometry vertices
class Face
{
    public int v1, v2, v3;
    
    //initialise face object
    public Face(int a, int b, int c)
    {
        v1 = a;
        v2 = b;
        v3 = c;
    }
    
    //return string representation of face
    @Override
    public String toString()
    {
        return "f "+v1+" "+v2+" "+v3;
    }       
}