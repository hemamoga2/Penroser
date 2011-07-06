package org.jf.Penroser;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class FatHalfRhombus extends HalfRhombus {
    private static final int TOP_FAT = 0;
    private static final int SKINNY = 1;
    private static final int BOTTOM_FAT = 2;

    private static final int NUM_CHILDREN = 3;
    private static final float[] leftVertices = new float[] {
            0.0f, 0.0f,
            EdgeLength.x(0, -2), EdgeLength.y(0, -2),
            0, EdgeLength.y(0, -2)*2
    };

    private static final float[] rightVertices = new float[] {
            0.0f, 0.0f,
            EdgeLength.x(0, 2), EdgeLength.y(0, 2),
            0, EdgeLength.y(0, 2)*2
    };

    //light blue
    private static final int leftColor = 0x527DFF;

    //dark blue
    private static final int rightColor = 0x0037DB;

    private HalfRhombus[] children = new HalfRhombus[NUM_CHILDREN];

    public FatHalfRhombus(int level, int side, float x, float y, float scale, int rotation) {
        super(level, side, x, y, scale, rotation);
    }

    @Override
    public void draw(GL11 gl, int maxLevel) {
        if (this.level < maxLevel) {
            for (int i=0; i<NUM_CHILDREN; i++) {
                HalfRhombus halfRhombus = getChild(i);
                halfRhombus.draw(gl, maxLevel);
            }
        } else {
            gl.glPushMatrix();
            gl.glTranslatef(this.x, this.y, 0);
            gl.glScalef(this.scale, this.scale, 0);
            gl.glRotatef(getRotationInDegrees(), 0, 0, -1);

            float[] vertices;
            int color;
            if (side == LEFT) {
                vertices = leftVertices;
                color = leftColor;
            } else {
                vertices = rightVertices;
                color = rightColor;
            }

            FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            vertexBuffer.put(vertices).position(0);

            gl.glVertexPointer(2, gl.GL_FLOAT, 0,  vertexBuffer);
            gl.glEnableClientState(gl.GL_VERTEX_ARRAY);

            gl.glColor4ub((byte)((color >> 16) & 0xFF), (byte)((color >> 8) & 0xFF), (byte)(color & 0xFF), (byte)0xFF);

            gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

            gl.glPopMatrix();
        }
    }

    @Override
    public HalfRhombus getChild(int i) {
        if (i<0 || i>=NUM_CHILDREN) {
            return null;
        }

        if (children[i] != null) {
            return children[i];
        }

        int sign = this.side==LEFT?1:-1;

        float topVerticeX = x + EdgeLength.x(level, rotation-(sign*2)) + EdgeLength.x(level, rotation+(sign*2));
        float topVerticeY = y + EdgeLength.y(level, rotation-(sign*2)) + EdgeLength.y(level, rotation+(sign*2));

        float sideVerticeX = x+EdgeLength.x(level, rotation-(sign*2));
        float sideVerticeY = y+EdgeLength.y(level, rotation-(sign*2));

        float newScale = scale / Constants.goldenRatio;

        switch (i) {
            case TOP_FAT:
                //180 degree rotation - we don't care about sign
                children[i] = new FatHalfRhombus(level+1, oppositeSide(), topVerticeX, topVerticeY, newScale, rotation+10);
                break;
            case SKINNY:
                children[i] = new SkinnyHalfRhombus(level+1, oppositeSide(), sideVerticeX, sideVerticeY, newScale, rotation+(sign*2));
                break;
            case BOTTOM_FAT:
                children[i] = new FatHalfRhombus(level+1, this.side, sideVerticeX, sideVerticeY, newScale, rotation+(sign*8));
                break;
        }

        return children[i];
    }
}
