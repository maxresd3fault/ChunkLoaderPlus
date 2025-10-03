package net.minecraft.src;

public class ParticleSparkle extends EntityFX {
	
	private int texIndex;
	
	public ParticleSparkle(World w, double particleX, double particleY, double particleZ, double velocityX, double velocityY, double velocityZ) {
		super(w, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.particleRed = 0.2F;
		this.particleGreen = 0.6F;
		this.particleBlue = 1.0F;
		this.particleGravity = 0.0F;
		this.particleMaxAge = 20 + w.rand.nextInt(10);
	}
	
	public void renderParticle(Tessellator tessellator, float partialTick, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		this.particleTextureIndex = this.texIndex;
		super.renderParticle(tessellator, partialTick, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}
	
	public float getEntityBrightness(float partialTick) {
		return 1.0F;
	}
	
	public int getFXLayer() {
		return 0;
	}
}