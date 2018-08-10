package com.example.andreidailida.arkanoidlite;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ArkanoidLite extends Activity {

    ArkanoidView arkanoidView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arkanoidView = new ArkanoidView(this);
        setContentView(arkanoidView);

    }

    class ArkanoidView extends SurfaceView implements Runnable {

        // Main thread
        Thread gameThread = null;

        SurfaceHolder ourHolder;

        // If playing or not
        volatile boolean playing;

        // Pause at start and in the end
        boolean paused = true;

        Canvas canvas;
        Paint paint;

        // FPS
        long fps;
        private long timeThisFrame;

        // Screen size
        int screenX;
        int screenY;

        // Platform
        Platform platform;

        // Ball
        Ball ball;

        // 40 Bricks
        Brick[] bricks = new Brick[40];
        int numBricks = 0;

        // For using sounds
        SoundPool soundPool;
        int bounceSound  = -1;
        int lostLifeSound = -1;
        int scoreSound = -1;

        // Score
        int score = 0;

        // Lives at the beginning
        int lives = 3;


        public ArkanoidView(Context context) {

            super(context);

            ourHolder = getHolder();
            paint = new Paint();

            Display display = getWindowManager().getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            // Create platform
            platform = new Platform(screenX, screenY);

            // Create a ball
            ball = new Ball();

            // Sounds
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

            try{
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor descriptor;

                descriptor = assetManager.openFd("bounce.ogg");
                bounceSound = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("lostLive.ogg");
                lostLifeSound = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("score.ogg");
                scoreSound = soundPool.load(descriptor, 0);

            }catch(Exception e){
                e.printStackTrace();
            }

            createBricksAndRestart();

        }

        public void createBricksAndRestart(){

            // Set ball and platform to start position
            ball.reset(screenX, screenY);
            platform = new Platform(screenX, screenY);

            // Set width and height of bricks
            int brickWidth = screenX / 10;
            int brickHeight = screenY / 10;
            // Set score to 0
            score = 0;

            // Creating bricks
            numBricks = 0;
            for(int column = 1; column < 9; column ++ ){
                for(int row = 1; row < 5; row ++ ){
                    bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                    numBricks ++;
                }
            }

            // If end of game or player won
            if(lives == 0 || score == numBricks * 100) {
                score = 0;
                lives = 3;
            }

        }

        @Override
        public void run() {
            while (playing) {

                long startFrameTime = System.currentTimeMillis();

                if(!paused){
                    update();
                }

                // Draw everything
                draw();

                // Calculate the fps this frame
                // USes to create animation
                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }

            }

        }

        // Updating method
        public void update() {

            platform.update(fps);

            // If platform out of the screen - stop moving
            if((platform.getX() <= -platform.getLength() / 2) || (platform.getX() >= screenX - platform.getLength() / 2 )) {
                platform.setMovementState(platform.STOPPED);
            }


            ball.update(fps);

            // Check for ball colliding with a brick
            for(int i = 0; i < numBricks; i++){

                if (bricks[i].getVisibility()){

                    // If colliding - add score and increase ball speed
                    if(RectF.intersects(bricks[i].getRect(), ball.getRect())) {
                        bricks[i].setInvisible();
                        ball.reverseYVelocity();

                        if (score % 2 == 0) {
                            ball.increaseSpeed();
                            platform.increaseSpeed();
                        }

                        score = score + 100;

                        soundPool.play(scoreSound, 1, 1, 0, 0, 1);
                    }
                }
            }

            // Check for ball colliding with platform
            if(RectF.intersects(platform.getRect(),ball.getRect())) {
                ball.setRandomXVelocity();
                ball.reverseYVelocity();
                ball.clearObstacleY(platform.getRect().top - 2);
                Log.i("TUT", String.valueOf(platform.getRect().top));
                Log.i("TUT", String.valueOf(ball.getRect().bottom));
                soundPool.play(bounceSound, 1, 1, 0, 0, 1);
            }

            // Bounce the ball back when it hits the bottom of screen
            if(ball.getRect().bottom > screenY){
                Log.i("TUT", String.valueOf(platform.getRect().top));
                Log.i("TUT", String.valueOf(ball.getRect().bottom));
                ball.reverseYVelocity();
               // ball.clearObstacleY(screenY - 2);
                ball.reset(screenX, screenY);

                lives --;
                platform = new Platform(screenX, screenY);

                paused = true;
                soundPool.play(lostLifeSound, 1, 1, 0, 0, 1);

            }

            // Bounce the ball back when it hits the top of screen
            if(ball.getRect().top < 0){
                ball.reverseYVelocity();
                ball.clearObstacleY(12);

                soundPool.play(bounceSound, 1, 1, 0, 0, 1);
            }

            // If the ball hits left wall bounce
            if(ball.getRect().left < 0){
                ball.reverseXVelocity();
                ball.clearObstacleX(2);

                soundPool.play(bounceSound, 1, 1, 0, 0, 1);
            }

            // If the ball hits right wall bounce
            if(ball.getRect().right > screenX - 10){
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 22);

                soundPool.play(bounceSound, 1, 1, 0, 0, 1);
            }

            // Pause if cleared screen
            if(score == numBricks * 100 || lives == 0){
                paused = true;
            }

        }

        // Draw objects
        public void draw() {

            // We can draw only if surface is valid
            if (ourHolder.getSurface().isValid()) {

                canvas = ourHolder.lockCanvas();

                // Draw the background
                Bitmap background = BitmapFactory.decodeResource(this.getResources(), R.drawable.background);
                background = Bitmap.createScaledBitmap(background, screenX, screenY, true);
                canvas.drawBitmap(background, 0, 0, null);

                // Draw the platform
                Bitmap paltformImg = BitmapFactory.decodeResource(this.getResources(), R.drawable.platform);
                paltformImg = Bitmap.createScaledBitmap(paltformImg, 130, 30, true);
                canvas.drawBitmap(paltformImg, platform.getX(), screenY-30, null);

                // Draw the ball
                canvas.drawRect(ball.getRect(), paint);

                // Draw the bricks if visible
                for(int i = 0; i < numBricks; i++){
                    if(bricks[i].getVisibility()) {
                        Bitmap brikImg = BitmapFactory.decodeResource(this.getResources(), R.drawable.brik);
                        brikImg = Bitmap.createScaledBitmap(brikImg, screenX / 10, screenY / 10, true);
                        canvas.drawBitmap(brikImg, bricks[i].getRect().centerX() - screenX / 10 / 2, bricks[i].getRect().centerY() - screenY / 10 /2, null);
                    }
                }

                // Choose white color to draw score and lives
                paint.setColor(Color.argb(255,  255, 255, 255));

                // Draw the score
                paint.setTextSize(40);
                canvas.drawText("Score: " + score + "   Lives: " + lives, 10,50, paint);

                // If Won
                if(score == numBricks * 100 && paused){
                    paint.setTextSize(70);
                    canvas.drawText("END OF GAME, YOU WON!", (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) , screenY /2 , paint);
                }
                // If lost
                if(lives == 0 && paused){
                    paint.setTextSize(70);
                    canvas.drawText("GAME OVER, TRY AGAIN!", (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) , screenY/2, paint);

                }

                // Draw everything
                ourHolder.unlockCanvasAndPost(canvas);
            }

        }

        // pause
        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        // resume afret pause
        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        // If touch - resume
        // Also implement moving
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {

                // Screen touched
                case MotionEvent.ACTION_DOWN:

                    paused = false;

                    if(score == numBricks*100 || lives == 0)
                        createBricksAndRestart();

                    if((motionEvent.getX() > screenX / 2) && (platform.getX() <= screenX-platform.getLength() / 2)){
                        platform.setMovementState(platform.RIGHT);
                    }
                    if((motionEvent.getX() < screenX / 2) && (platform.getX() >= -platform.getLength() / 2)){
                        platform.setMovementState(platform.LEFT);
                    }
                    break;

                // Not touching any more
                case MotionEvent.ACTION_UP:

                    platform.setMovementState(platform.STOPPED);
                    break;
            }
            return true;
        }

    }

    // Player starts the game
    @Override
    protected void onResume() {
        super.onResume();
        arkanoidView.resume();
    }

    // Quits the game
    @Override
    protected void onPause() {
        super.onPause();
        arkanoidView.pause();
    }

}
