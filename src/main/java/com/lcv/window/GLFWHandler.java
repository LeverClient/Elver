package com.lcv.window;

import com.lcv.Main;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLFWHandler {
    public long window;

    public final Object lock = new Object();

    public int width;

    public int height;


    public int directShaderProgram;

    public int directShaderProgram2;

    public int vao;

    public int vao2;

    public int ebo;


    public void init(int width, int height) throws IOException {
        this.width = width;
        this.height = height;

        // setup window windoow settings
        glfwInit();

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        glfwWindowHint(GLFW_FOCUSED, 1);
        glfwWindowHint(GLFW_DECORATED, 1);
        glfwWindowHint(GLFW_RESIZABLE, 0);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, 1);

        // create window
        window = glfwCreateWindow(width, height, "meow", NULL, NULL);

        // setup rendering i guess
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // v-sync
        glfwShowWindow(window);

        GL.createCapabilities();

        // input and stuff (very necessary)
        setupCallbacks();

        // setup GLSL shaders
        initShaders();

        // setup stuff necessary for drawing
        setupDraw();
    }

    private int compileShader(int type, String shaderFile) throws IOException {
        try (InputStream sourceStream = Main.class.getResourceAsStream(shaderFile)) {
            if (sourceStream == null) {
                System.err.printf("Failed to read shader \"%s\" This is probably very bad%n", shaderFile);
                return -1;
            }

            int id = glCreateShader(type);
            glShaderSource(id, new String(sourceStream.readAllBytes()));
            glCompileShader(id);

            // check for compile error
            int[] success = {0};
            glGetShaderiv(id, GL_COMPILE_STATUS, success);

            if (success[0] == 0) {
                String failReason = glGetShaderInfoLog(id);
                System.err.printf("Failed to compile shader \"%s\" This is probably very bad%n", shaderFile);
                System.err.println(failReason);

                return -1;
            }

            return id;
        }
    }

    private int createShaderProgram(String name, int... shaders) {
        int program = glCreateProgram();

        for (int shader : shaders) {
            glAttachShader(program, shader);
        }

        glLinkProgram(program);

        // check for error
        int[] success = {0};
        glGetProgramiv(program, GL_LINK_STATUS, success);

        if (success[0] == 0) {
            String failReason = glGetProgramInfoLog(program);
            System.err.printf("Failed to compile program \"%s\" This is probably very bad%n", name);
            System.err.println(failReason);

            return -1;
        }

        return program;
    }

    public void initShaders() throws IOException {
        int vertexDirectShader = compileShader(GL_VERTEX_SHADER, "/GLSL/Vertex/Direct.glsl");
        int fragDirectShader = compileShader(GL_FRAGMENT_SHADER, "/GLSL/Fragment/Direct.glsl");
        int fragDirectShader2 = compileShader(GL_FRAGMENT_SHADER, "/GLSL/Fragment/Direct2.glsl");

        directShaderProgram = createShaderProgram("Direct",  vertexDirectShader, fragDirectShader);
        directShaderProgram2 = createShaderProgram("Direct2",  vertexDirectShader, fragDirectShader2);

        glDeleteShader(vertexDirectShader);
        glDeleteShader(fragDirectShader);
        glDeleteShader(fragDirectShader2);
    }

    public int polygonMode = GL_FILL;
    public void setupCallbacks() {
        glfwSetKeyCallback(window, (long window, int key, int scancode, int action, int mods) -> {
            if (action != GLFW_RELEASE) {
                return;
            }

            switch(key) {
                case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, true);
                case GLFW_KEY_ENTER -> {
                    polygonMode++;
                    if (polygonMode > GL_FILL) {
                        polygonMode = GL_POINT;
                    }

                    glPolygonMode(GL_FRONT_AND_BACK, polygonMode);
                }
            }
        });
    }

    public void setupDraw() {
//        float[] vertices = {
//                -0.5f, 0.5f, 0.0f, // top left
//                -0.5f, -0.5f, 0f, // bottom left
//                0.5f, -0.5f, 0f, // bottom right
//                0.5f, 0.5f, 0.0f, // top right
//        };

//        float[] vertices = {
//                0f - .33f, 0.5f - .33f, 0.0f - .33f, // top
//                -0.5f - .33f, -0.5f - .33f, - .33f, // bottom left
//                0.5f - .33f, -0.5f - .33f, 0f - .33f, // bottom right
//        };
//
//        float[] vertices2 = {
//                -0.5f + .33f, 0.5f + .33f, 0f + .33f, // top left
//                0.5f + .33f, 0.5f + .33f, 0f + .33f, // top right
//                0f + .33f, -0.5f + .33f, 0.0f + .33f, // bottom
//        };

        float[] vertices = {
                0f, 0.5f, 0.0f, // top
                -0.5f, -0.5f, 0f, // bottom left
                0.5f, -0.5f, 0f, // bottom right
        };

        float[] vertices2 = {
                -0.5f, 0.5f, 0f, // top left
                0.5f, 0.5f, 0f, // top right
                0f, -0.5f, 0.0f, // bottom
        };

        int[] indexes = {
                0, 1, 2,
                0, 3, 2,
        };


        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

//        ebo = glGenBuffers();
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
//        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexes, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        vao2 = glGenVertexArrays();
        glBindVertexArray(vao2);

        int vbo2 = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo2);
        glBufferData(GL_ARRAY_BUFFER, vertices2, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    }

    private double total = 0d;
    public void draw(double delta) {
        glViewport(0, 0, width, height);
        glClearColor(0.2f, 0.2f, 0.2f, 0.5f);
        glClear(GL_COLOR_BUFFER_BIT);

        //glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glEnable(GL_ALPHA);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_COLOR, GL_DST_COLOR);
        // glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);

        glUseProgram(directShaderProgram);

        glBindVertexArray(vao);

        total += delta;
        float offsetY = (float) Math.sin(total * 2) * 1/3;
        float offsetX = (float) Math.sin(total * 4) * 1/3;

        float[] vertices = {
                0f+offsetX, 0.5f+offsetY, 0.0f, // top
                -0.5f+offsetX, -0.5f+offsetY, 0f, // bottom left
                0.5f+offsetX, -0.5f+offsetY, 0f, // bottom right
        };
        glBindBuffer(GL_ARRAY_BUFFER, vao);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

        glDrawArrays(GL_TRIANGLES, 0, 3);


        glUseProgram(directShaderProgram2);

        glBindVertexArray(vao2);

        float[] vertices2 = {
                -0.5f-offsetX, 0.5f-offsetY, 0f, // top left
                0.5f-offsetX, 0.5f-offsetY, 0f, // top right
                0f-offsetX, -0.5f-offsetY, 0.0f, // bottom
        };
        glBindBuffer(GL_ARRAY_BUFFER, vao2);
        glBufferData(GL_ARRAY_BUFFER, vertices2, GL_DYNAMIC_DRAW);

        glDrawArrays(GL_TRIANGLES, 0, 3);

        glBindVertexArray(0);
    }

    public void loop() {
        double lastFrame = glfwGetTime();
        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            double delta = now - lastFrame;

            glfwPollEvents();
            draw(delta);

            glfwSwapBuffers(window);

            lastFrame = now;
        }

        glfwTerminate();
    }
}
