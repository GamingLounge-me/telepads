package de.jonas.telepads.particle.effects;

import de.jonas.telepads.particle.ParticleEffect;
import de.jonas.telepads.particle.ParticleRunner;
import de.jonas.telepads.particle.ParticleSpawner;

public class SpiralEffect implements ParticleEffect {
    private int number;
    private double rotations;
    private double height;
    private ParticleSpawner spawner;

    /**
     * Creates a Spiral Effect
     * 
     * @param number    of Spiralparts
     * @param rotations how many full rotations
     * @param height    of the effect in blocks relative to effect location
     * @param spawner   for the particles
     */
    public SpiralEffect(int number, double rotations, double height, ParticleSpawner spawner) {
        this.number = number;
        this.rotations = rotations;
        this.height = height;
        this.spawner = spawner;
    }

    public void tick(ParticleRunner runner, double progress) {
        double numberOffset = 2 * Math.PI / (double) number;
        double rotProgress = progress * 2 * Math.PI * rotations;
        double yo = progress * height;
        for (int i = 0; i < number; i++) {
            double phi = rotProgress + i * numberOffset;
            spawner.spawn(runner, progress, Math.cos(phi), yo, Math.sin(phi));
        }
    }
}
