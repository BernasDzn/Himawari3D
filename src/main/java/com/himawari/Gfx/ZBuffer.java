package com.himawari.Gfx;

import java.util.Arrays;

import com.himawari.HLA.Vec3;
import com.himawari.Utils.Window;

public class ZBuffer {
    
    public static int bufferWidth, bufferHeight;
    public static float[] depthBuffer;

    // initialize the buffer with the respective window dimensions
    public static void Init(){

        ZBuffer.bufferWidth = Window.width;
        ZBuffer.bufferHeight = Window.height;

        ZBuffer.depthBuffer = new float[bufferWidth*bufferHeight];

        ClearDepthBuffer();
    }

    // Overwrite (or not) current depth value
    public static boolean TestAndSet(int x, int y, float depth){

        float cachedDepth = depthBuffer[x + y * bufferWidth];
        if (depth < cachedDepth) {
            depthBuffer[x + y * bufferWidth] = depth;
            return true;
        }

        return false;
    }

    // Given a 3D plane, correctly project the point to the matching Z axis
    public static Vec3 ProjectOntoToFace(Vec3 vertex1, Vec3 vertex2, Vec3 vertex3, Vec3 point){

        Vec3 v1 = vertex2.copy().subtract(vertex1);
        Vec3 v2 = vertex3.copy().subtract(vertex1);

        Vec3 planeCoefficients = Vec3.CrossProduct(v1, v2);
        float planeConstant = planeCoefficients.copy().dot(vertex1).sum();

        float zComponent = (planeConstant - point.x*planeCoefficients.x - point.y * planeCoefficients.y) / planeCoefficients.z;
        point.z = zComponent;

        return point;
    }

    // Fill the depth buffer with infinity
    public static void ClearDepthBuffer(){
        Arrays.fill(depthBuffer, 9999);
    }
}
