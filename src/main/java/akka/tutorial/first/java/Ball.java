package akka.tutorial.first.java;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Circle;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import java.awt.geom.Point2D;

public class Ball {
	//JavaFX UI for ball.
	public Node node;
	
	//x and y position
	private Point2D.Float point;
	private float x;
	private float y;
	
	private BodyType bodyType;
	
	private LinearGradient linearGradient;
	
	private float radius;
	
	public Ball( float x, float y ) {
		this( x, y, (new Integer(Utils.BALL_SIZE)).floatValue(), BodyType.DYNAMIC, Color.BLUE);
	}
	
	public Ball(float x, float y, float radius, BodyType bodyType, Color color) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.bodyType = bodyType;
		this.linearGradient = Utils.getBallGradient(color);
        node = create();		
	}
	
	private Node create() {
		Circle ball = new Circle();
		ball.setRadius(radius);
		ball.setFill(linearGradient);
		
		ball.setLayoutX(Utils.toPixelPosX(x));
		ball.setLayoutY(Utils.toPixelPosY(y));
		
		ball.setCache(true);
		
		//Create a JBox2D body definition for ball.
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(x, y);
		
        CircleShape circleShape = new CircleShape();
        circleShape.m_radius = radius*0.1f;
        
        // Create a fixture for ball
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 0.9f;
        fixtureDef.friction = 0.3f;       
        fixtureDef.restitution = 0.7f;
        
        Body body = Utils.world.createBody(bodyDef);
        body.createFixture(fixtureDef);
        ball.setUserData(body);
		return ball;
	}
}
