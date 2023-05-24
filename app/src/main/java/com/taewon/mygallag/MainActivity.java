package com.taewon.mygallag;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    private Intent userIntent;
    ArrayList<Integer> bgMusicList;
    public static SoundPool effectSound;
    public static float effectVolumn;
    ImageButton specialShotBtn;
    public static ImageButton fireBtn, reloadBtn;
    JoystickView joyStick;
    public static TextView scoreTv;
    LinearLayout gameFrame;

    ImageView pauseBtn;
    public static LinearLayout lifeFrame;
    SpaceInvadersView spaceInvadersView;
    public static MediaPlayer bgMusic;    int bgMusicIndex;
    public static TextView bulletCount;
    private static ArrayList<Integer> effectSoundList;
    public static final int PLAYER_SHOT = 0;
    public static final int PLAYER_HURT = 1;
    public static final int PLAYER_RELOAD = 2;
    public static final int PLAYER_GET_ITEM = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userIntent = getIntent();
        bgMusicIndex = 0;
        bgMusicList = new ArrayList<Integer>(); // 음악 넣을 arrayList 생성
        bgMusicList.add(R.raw.main_game_bgm1);
        bgMusicList.add(R.raw.main_game_bgm2);
        bgMusicList.add(R.raw.main_game_bgm3);

        effectSound = new SoundPool(5, AudioManager.USE_DEFAULT_STREAM_TYPE, 0);
        effectVolumn = 1;

        specialShotBtn = findViewById(R.id.specialShotBtn); // 번개
        joyStick = findViewById(R.id.joyStick);
        scoreTv = findViewById(R.id.score); // TextView
        fireBtn = findViewById(R.id.fireBtn); // ImageButton
        reloadBtn = findViewById(R.id.reloadBtn); // ImageButton
        gameFrame = findViewById(R.id.gameFrame); // 첫번째 LinearLayout
        pauseBtn = findViewById(R.id.pauseBtn); // ImageButton
        lifeFrame = findViewById(R.id.lifeFrame); // 윗부분 LinearLayout

        init();
        setBtnBehavior(); // 조이스틱 작동함수 실행
    }

    @Override
    protected void onResume() {
        super.onResume();
        bgMusic.start();
        spaceInvadersView.resume();
    }

    private void init() {
        Display display = getWindowManager().getDefaultDisplay(); // view의 display를 얻어온다.
        Point size = new Point();
        display.getSize(size);

        // ??? 아이템 화면에 띄우기
        spaceInvadersView = new SpaceInvadersView(this, userIntent.getIntExtra("character", R.drawable.ship_0000) , size.x, size.y);
        gameFrame.addView(spaceInvadersView); // 프레임에 만든 아이템들 넣기

        // 음악 바꾸기
       changeBgMusic();
        bgMusic.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { // bgMusic의 음악이 끝났으면
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                changeBgMusic();
            }
        });

        bulletCount = findViewById(R.id.bulletCount); // 총알 개수

        // spaceInvadersView의 getPlayer() 구현
        bulletCount.setText(spaceInvadersView.getPlayer().getBulletsCount() + "/30"); // 30/30 표시
        scoreTv.setText(Integer.toString(spaceInvadersView.getScore())); // score: 0 표시

        effectSoundList = new ArrayList<>();
        effectSoundList.add(PLAYER_SHOT, effectSound.load(MainActivity.this, R.raw.player_shot_sound, 1));
        effectSoundList.add(PLAYER_HURT, effectSound.load(MainActivity.this, R.raw.player_hurt_sound, 1));
        effectSoundList.add(PLAYER_RELOAD, effectSound.load(MainActivity.this, R.raw.reload_sound, 1));
        effectSoundList.add(PLAYER_GET_ITEM, effectSound.load(MainActivity.this, R.raw.player_get_item_sound, 1));
        bgMusic.start(); // 음악이 바뀌면서 재생
    }

    private void changeBgMusic() {
        bgMusic = MediaPlayer.create(this, bgMusicList.get(bgMusicIndex)); // 음악하나
        bgMusic.start();
        bgMusicIndex++; // 음악 바꾸기 위해 증가시켜놓음
        bgMusicIndex = bgMusicIndex % bgMusicList.size(); // 나머지: 음악 개수만큼만 바뀌게 하기 위해
    }

    @Override
    protected void onPause() { // 일시 정지 시
        super.onPause();
        bgMusic.pause();
        spaceInvadersView.pause();
    }

    public static void effectSound(int flag) {
        effectSound.play(effectSoundList.get(flag), effectVolumn, effectVolumn,
                0, 0, 1.0f);
        // soundpool 실행 (재생 우선순위, 반복재생 여부, 재생 속도)
   }

   private void setBtnBehavior() {
        joyStick.setAutoReCenterButton(true); // joyStick이 자동으로 중앙으로 원위치 오게 하는 기능을 활성화
        joyStick.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                Log.d("keycode", Integer.toString(i));
                return false;
            }
        });

        // 조이스틱 이동방향으로 비행기 이동하게 한다
       joyStick.setOnMoveListener(new JoystickView.OnMoveListener() {
           @Override
           public void onMove(int angle, int strength) {
               Log.d("angle", Integer.toString(angle));
               Log.d("force", Integer.toString(strength));
               if (angle > 67.5 && angle < 112.5) {
                   // 위
                   spaceInvadersView.getPlayer().moveUp(strength / 10);
                   spaceInvadersView.getPlayer().resetDx();
               } else if (angle > 247.5 && angle < 292.5) {
                   // 아래
                   spaceInvadersView.getPlayer().moveDown(strength / 10);
                   spaceInvadersView.getPlayer().resetDx();
               } else if (angle > 112.5 && angle < 157.5) {
                   // 왼쪽 대각선 위
                   spaceInvadersView.getPlayer().moveUp(strength / 10 * 0.5);
                   spaceInvadersView.getPlayer().moveLeft(strength / 10 * 0.5);
               } else if (angle > 157.5 && angle < 202.5) {
                   // 왼쪽
                   spaceInvadersView.getPlayer().moveLeft(strength / 10);
                   spaceInvadersView.getPlayer().resetDy();
               } else if (angle > 202.5 && angle < 247.5) {
                   // 왼쪽 대각선 아래
                   spaceInvadersView.getPlayer().moveLeft(strength / 10 * 0.5);
                   spaceInvadersView.getPlayer().moveDown(strength / 10 * 0.5);
               } else if (angle > 22.5 && angle < 67.5) {
                   // 오른쪽 대각선 위
                   spaceInvadersView.getPlayer().moveUp(strength / 10 * 0.5);
                   spaceInvadersView.getPlayer().moveRight(strength / 10 * 0.5);
               } else if (angle > 337.5 && angle < 22.5) {
                   // 오른쪽
                   spaceInvadersView.getPlayer().moveRight(strength / 10);
                   spaceInvadersView.getPlayer().resetDy();
               } else if (angle > 292.5 && angle < 22.5) {
                   // 오른쪽 아래
                   spaceInvadersView.getPlayer().moveRight(strength / 10 * 0.5);
                   spaceInvadersView.getPlayer().moveDown(strength / 10 * 0.5);
               }
           }
       });

       fireBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               spaceInvadersView.getPlayer().fire();
           }
       });

       reloadBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               spaceInvadersView.getPlayer().reloadBullets();
           }
       });

       pauseBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               spaceInvadersView.pause(); // spaceInvadersView 일시정지
               PauseDialog pauseDialog = new PauseDialog(MainActivity.this);
               pauseDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                   @Override
                   public void onDismiss(DialogInterface dialogInterface) {
                       spaceInvadersView.resume(); // spaceInvadersView 종료
                   }
               });
               pauseDialog.show(); // pauseDialog 띄움
           }
       });

       specialShotBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if (spaceInvadersView.getPlayer().getSpecialShotCount() >= 0)
                   spaceInvadersView.getPlayer().specialShot();
           }
       });
   }
}