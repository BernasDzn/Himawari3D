package com.himawari;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.himawari.geom.Vec3;

public class Mesh {

    public static final String VERTEX_CUE = "v";
    public static final String FACE_CUE = "f";
    
    public Vec3[] vertices;
    public Vec3[][] faces;

    public static Mesh LoadFrom(String filename){

        try {

            List<Vec3> vertices = new ArrayList<Vec3>();
            List<Vec3[]> faces = new ArrayList<Vec3[]>();
            
            String[] text = Utils.GetFileContents(filename).split("\n");
            for (String line : text) {
                
                // Get the line indicator at the start of each line
                if(line.length()==0) continue;
                char indicator = line.charAt(0);

                // Ignore comments
                if(indicator == '#') continue;

                // Divide the data
                String[] values = line.split(" ");

                switch (values[0]) {
                    case VERTEX_CUE:

                        Vec3 vertex = new Vec3();
                        vertex.x = Float.parseFloat(values[1]);
                        vertex.y = Float.parseFloat(values[2]);
                        vertex.z = Float.parseFloat(values[3]);

                        vertex.scale(30);
                        vertex.sum(Vec3.from(100));

                        vertices.add(vertex);

                        break;         
                    case FACE_CUE:

                        Vec3[] faceStructure = new Vec3[3];
                        faceStructure[0] = ParseFace(values[1]);
                        faceStructure[1] = ParseFace(values[2]);
                        faceStructure[2] = ParseFace(values[3]);

                        faces.add(faceStructure);
                        break;
                }
            }

            // Initialize mesh
            Mesh mesh = new Mesh();
            mesh.vertices = vertices.toArray(new Vec3[vertices.size()]);
            
            // Transform list of arrays to matrix
            mesh.faces = new Vec3[faces.size()][3];
            mesh.faces = faces.toArray(mesh.faces);

            // for (int i = 0; i < mesh.faces.length; i++) {
            //     for (int j = 0; j < mesh.faces[i].length; j++) {
            //         if(mesh.faces[i][j] == null) System.out.print("null ");
            //         else System.out.print(mesh.faces[i][j].x + ", " + mesh.faces[i][j].y + ", " + mesh.faces[i][j].z + " ");
            //     }
            //     System.out.println();
            // }

            return mesh;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Vec3 ParseFace(String text) {

        Float[] map = Arrays.asList(text.split("/")).stream().map(Float::valueOf).toArray(Float[]::new);
        return new Vec3(
                map[0].floatValue(),
                map[1].floatValue(),
                map[2].floatValue()
            );
    }
}
