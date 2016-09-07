package marc.passingStones;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Random;

// Android Studio nice shortcuts
// command+d duplicate current line
// command+space autocomplete
// shit+f6 refractor-rename
// control+alt+o organize imports
// control+alt+i smart indent
// command+R replace (+shit replace in entire path)


public class Main extends Activity {

	public static final float V = 9;
	private Pot[][] pots= new Pot[2][5];
	public int initialStones =2;
	private static int[] idColors={R.string.COLOR_0,R.string.COLOR_1,R.string.COLOR_2};
	private static int[] idColorsPots={R.string.COLOR_0_POTS,R.string.COLOR_1_POTS,R.string.COLOR_2_POTS};
	String color="purple";
	String ref_color;
	public Object vy;
	public boolean gameEnd =false;


	@Override
	public void onCreate(Bundle b) {
		requestWindowFeature(Window.FEATURE_NO_TITLE); // title bar out
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(b);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		Bundle bundle = getIntent().getExtras();
		initialStones = Integer.parseInt(bundle.getString("initialStones"));
		
		initPots();
//		Log.d("Main","al main!");
		View v = new gamefieldView(this);
		
		setContentView(v);

	}

	protected void initPots() {
		for(int j=0;j<2;j++) 
		{ 
			for(int p=0;p<5;p++)  // 4+1 pots per player
			{
				if(p==4) pots[j][p]=new Pot(j, 0);
				else	pots[j][p]=new Pot(j, initialStones);
			}
		}
	}

	public class gamefieldView extends View{

		private Bitmap[][] bitmapPot = new Bitmap[3][5];
		private Bitmap mBG;
		protected boolean onMoving = false;
		protected float xcur;
		protected float ycur;
		private Bitmap mBag;
		Paint paint;
		int[] potSel;
		int potHeight ;
		int potWidth ;
		int stones = 0;
		//	
		protected float xFinal;
		protected float yFinal;
		private float vx,vy;
		protected int turn;

		public gamefieldView(Context context) {
			super(context);
			
			for(int j=0;j<3;j++)  //assign to each color i the number of stones of an image
				// e.g. bitmapPot[1][2] is red pot with 2 stones
			{
				for(int p=0;p<=4;p++){
					color = getString(idColorsPots[j]);
					int tmp=getResources().getIdentifier("pot"+p+"_"+color, "drawable", "marc.passingStones");
					bitmapPot[j][p] = BitmapFactory.decodeResource(context.getResources(), tmp);
					if(bitmapPot[j][p]==null)  Log.d("Main","null resource"+p); 
					bitmapPot[j][p] = scaleBitmap(bitmapPot[j][p], 1.5f, 1.5f);
				}
			}
			potHeight = bitmapPot[0][0].getHeight();
			potWidth = bitmapPot[0][0].getWidth();

			mBG = BitmapFactory.decodeResource(getResources(), R.drawable.bg2);
			mBag = BitmapFactory.decodeResource(getResources(), R.drawable.bag);
			mBag = scaleBitmap(mBag, 0.1f, 0.1f);


			paint = new Paint();
			paint.setTextSize(40); 
			potSel = new int[2];
			potSel[0]=0; potSel[1]=0;
			
			turn = new Random().nextInt(2);

			color = getResources().getString(idColors[turn]);
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.START_MESSAGE)+color, Toast.LENGTH_SHORT).show();
			
			OnTouchListener OnTouch= new OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					if(!onMoving && !gameEnd){
						potSel = searchPot(event.getX(),event.getY());
						if(potSel[0]+potSel[1]<10){
							if(potSel[1]==4)
							   myToast(getResources().getString(R.string.NO_NEUTRAL));
							else if(potSel[0]!= turn)
								myToast(getResources().getString(R.string.NO_TURN));
							else{
								stones = pots[potSel[0]][potSel[1]].emptyPot();
								if(stones >0){
									newPositions();
									onMoving = true;
								}
								else 
									myToast(getResources().getString(R.string.NO_STONES));
							}
						}
					}
					else if(gameEnd){
						myToast(getContext().getString(R.string.IF_NEW_GAME));
					}

					return true;
				}
			};
			setOnTouchListener(OnTouch);
		}

		protected void onDraw(Canvas canvas) {
			float totalX = this.getWidth();
			float totalY = this.getHeight();

			drawStatic(canvas, totalX, totalY);
			
			if(onMoving)	newPositions(canvas);

			color = getResources().getString(idColors[turn]);
			setDrawText(Typeface.BOLD,Color.BLACK);
			canvas.drawText(getResources().getString(R.string.TURN)+" "+color,totalX*(1/2f-1/10f), totalY*(1/2f+1/15f), paint);
			postInvalidateDelayed(20);
			resetDrawText();
		}

		protected void newPositions(Canvas canvas) {
			canvas.drawBitmap(mBag, xcur, ycur,null);
			int sign =  (2*potSel[0]-1);
			if(potSel[1]==4) sign*=-1;
			
			xcur +=vx;
			ycur +=vy;

			if(xcur *sign>sign*(xFinal-potSel[0]* mBag.getWidth()))
			{
				//			pots adalt  xcur<xFinal
				//			pots adalt  xcur>xFinal-mBag.getWidth()

				potSel[1]=(potSel[1]+1)%5;
				if(potSel[1]==0)
					potSel[0] = (potSel[0]+1)%2;

				pots[potSel[0]][potSel[1]].incrementStones();
				stones -=1;
				if(stones ==0){
					onMoving=false; 
					for(int i=0;i<4;i++){  // var onMoving is reused to indicate
						if(pots[turn][i].getStones()>0) // if the player has still stones
							onMoving=true;
					}
					if(onMoving && potSel[1]<4) // still has stones, not ending
						// que el neutral
						turn = (turn +1)%2;
					else if(!onMoving){
						gameEnd = true;
						color = getString(idColors[turn]);
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.GAME_END)+color, Toast.LENGTH_SHORT).show();
					}
						
					onMoving=false;  // onMoving recovers its meaning
					}
				else			newPositions();
			}
		}

		protected void drawStatic(Canvas canvas, float totalX, float totalY) {
			canvas.save();

			float scaleX = totalX / mBG.getWidth();
			float scaleY = totalY / mBG.getHeight();
			canvas.scale(scaleX,scaleY);  // scale the canvas so that mBG occupies all screen

			canvas.drawBitmap(mBG,0,0,null); 

			canvas.restore();

			for(int j=0;j<2;j++){
				int sign = 2*j-1;  // 0 -> -1 ,  1 -> 1
				for(int p=0;p<4;p++){
					float X = (float) totalX/5*(4-3*j+sign*p);  // 0-> 4-p , 1-> 1+p
					float Y = (float)  totalY*(1+2*j)/4;
					pots[j][p].setXY(X,Y);
					canvas.drawBitmap(bitmapPot[j][Math.min(4,pots[j][p].getStones())],
							X-potWidth/2,Y-potHeight/2,null);
                    setDrawText(Typeface.BOLD, Color.WHITE);
					canvas.drawText(Integer.toString(pots[j][p].getStones()),
							X, Y+potHeight, paint);
                    resetDrawText();
				}
				float X = (float) pots[j][3].getX()+sign*potWidth;
				float Y = (float)  totalY*(2)/4;
				pots[j][4].setXY(X,Y);
				canvas.drawBitmap(bitmapPot[2][Math.min(4,pots[j][4].getStones())],
						X-potWidth/2,Y-potHeight/2,null);
                setDrawText(Typeface.BOLD, Color.WHITE);
				canvas.drawText(Integer.toString(pots[j][4].getStones()),
						X, Y+potHeight, paint);
                resetDrawText();

			}
		}
        public void setDrawText(int style,int color){
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, style));
            paint.setColor(color);

        }
        public void resetDrawText(){
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setColor(Color.BLACK);
        }

		public int[] searchPot(float posX, float posY){

			double minDist = Math.pow(bitmapPot[0][0].getWidth(),2)/3.5;
			int[] x={10,10};

			for(int j=0;j<2;j++){
				for(int p=0;p<5;p++){
					double dist = Math.pow( pots[j][p].getX()-posX,2)+Math.pow( pots[j][p].getY()-posY,2);
					if(dist<minDist){
						x[0]=j; x[1]=p; //minDist = dist;
					}
				}
			}

			return x;
		}

		protected void newPositions() {
			int nextPot=(potSel[1]+1)%5;
			int nextPlayer;
			if(nextPot>0)
				nextPlayer = potSel[0];
			else
				nextPlayer = (potSel[0]+1)%2;

			//int sign = 2*potSel[0]-1;

			xcur = (float) pots[potSel[0]][potSel[1]].getX();//+sign*bitmapPot[0][0].getWidth()/4f-potSel[0]*mBag.getWidth();
			ycur = (float) pots[potSel[0]][potSel[1]].getY();

			xFinal= (float) pots[nextPlayer][nextPot].getX();
			yFinal= (float) pots[nextPlayer][nextPot].getY();

			vx = Math.signum(xFinal - xcur)*V;
			vy = Math.signum(yFinal - ycur)*V;
			
		}

	}

	public static Bitmap scaleBitmap(Bitmap bitmapToScale, float WidthScale, float HeightScale) {   
		if(bitmapToScale == null)
			return null;
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map
		matrix.postScale(WidthScale, HeightScale);

		// recreate the new Bitmap and set it back
		return Bitmap.createBitmap(bitmapToScale, 0, 0, bitmapToScale.getWidth(), bitmapToScale.getHeight(), matrix, true);  
	}
	
	
	public void myToast(String missatge){
		final Toast toast = Toast.makeText(getBaseContext(), missatge,Toast.LENGTH_SHORT);
	    toast.show();
	    new CountDownTimer(900, 1000)
	    {
	        public void onTick(long millisUntilFinished) {toast.show();}
	        public void onFinish() {toast.cancel();}
	    }.start();
	}
	

}