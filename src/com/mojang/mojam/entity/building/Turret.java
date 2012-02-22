package com.mojang.mojam.entity.building;

import java.util.Set;

import com.mojang.mojam.entity.*;
import com.mojang.mojam.entity.mob.*;
import com.mojang.mojam.gui.Font;
import com.mojang.mojam.level.tile.Tile;
import com.mojang.mojam.screen.*;

public class Turret extends Building {

	private int delayTicks = 0;
	private int delay;
	private double radius;
	private double radiusSqr;

	private int[] upgradeRadius = new int[] { 3 * Tile.WIDTH, 5 * Tile.WIDTH,
			7 * Tile.WIDTH };
	private int[] upgradeDelay = new int[] { 24, 21, 18 };

	private int facing = 0;

	public Turret(double x, double y, int team) {
		super(x, y, team);
		setStartHealth(10);
		freezeTime = 10;

		makeUpgradeableWithCosts(new int[] { 500, 1000, 5000 });
	}

	public void init() {
	}

	public void tick() {
		super.tick();
		if (--freezeTime > 0)
			return;
		if (--delayTicks > 0)
			return;

		Set<Entity> entities = level.getEntities(pos.x - radius,
				pos.y - radius, pos.x + radius, pos.y + radius);

		
		Entity closest = null;
		double closestDist = 99999999.0f;
		for (Entity e : entities) {
			if (!(e instanceof Mob))
				continue;
			if ((e instanceof RailDroid))
				continue;
			if (!((Mob) e).isNotFriendOf(this))
				continue;
			if ((e instanceof TreasurePile))
				continue;
			final double dist = e.pos.distSqr(pos);
			if (dist < radiusSqr && dist < closestDist && !isTargetBehindWall(e.pos.x, e.pos.y)) {
			    closestDist = dist;
				closest = e;
			}
		}
		if (closest == null)
			return;

		double invDist = 1.0 / Math.sqrt(closestDist);
		double yd = closest.pos.y - pos.y;
		double xd = closest.pos.x - pos.x;
		double angle = (Math.atan2(yd, xd) + Math.PI * 1.625);
		facing = (8 + (int) (angle / Math.PI * 4)) & 7;
		Bullet bullet = new Bullet(this, xd * invDist, yd * invDist);
		bullet.pos.y -= 10;
		level.addEntity(bullet);
		
		if (upgradeLevel > 0){
		    Bullet second_bullet = new Bullet(this, xd * invDist, yd * invDist);
            level.addEntity(second_bullet);
            if (facing == 0 || facing == 4){
                bullet.pos.x -= 5;
                second_bullet.pos.x += 5;
            }
		}
		
		delayTicks = delay;
	}
	
	private boolean isTargetBehindWall(double targetPosX, double targetPosY){
	    
	    // work in progress
	    
	    int tileX = (int) Math.round((pos.x-298.0)/Tile.WIDTH);
        int tileY = (int) Math.round((pos.y-298.0)/Tile.HEIGHT);
        
        int targetTileX = (int) Math.round((targetPosX-298.0)/Tile.WIDTH);
        int targetTileY = (int) Math.round((targetPosY-298.0)/Tile.WIDTH);
        
 //       System.out.println("x: " + tileX  + "     y: " + tileY);
 //       System.out.println("targetTileX: " + targetTileX  + "     targetTileY: " + targetTileY);
        
        return false;
	}

	public void render(Screen screen) {
		super.render(screen);
		addHealthBar(screen);
	}

	private void addHealthBar(Screen screen){
	    
	    int bar_width = 30;
        int bar_height = 2;
        int start = health * bar_width / maxHealth;
        Bitmap bar = new Bitmap (bar_width, bar_height);
        
        bar.clear(0xff00ff00);
        bar.fill(start, 0, bar_width - start, bar_height, 0xffff0000);
        
        screen.blit(bar, pos.x - (bar_width/2), pos.y + 10);
	}
	
	public Bitmap getSprite() {
	    switch (upgradeLevel){
	    case 1:
	        return Art.turret2[facing][0];
	    case 2:
	        return Art.turret3[facing][0];
	    default:
	        return Art.turret[facing][0];
	    }
	}

	protected void upgradeComplete() {
	    maxHealth += 10;
	    health = maxHealth;
        delay = upgradeDelay[upgradeLevel];
		radius = upgradeRadius[upgradeLevel];
		radiusSqr = radius * radius;
	}
}
