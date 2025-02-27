package me.trent.skyblock.island;

import me.trent.skyblock.SkyBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public abstract class SpiralTask implements Runnable {

    // general task-related reference data
    private transient World world = null;
    private transient boolean readyToGo = false;
    private transient int taskID = -1;
    private transient int limit = 0;

    // values for the spiral pattern routine
    private transient int x = 0;
    private transient int z = 0;
    private transient boolean isZLeg = false;
    private transient boolean isNeg = false;
    private transient int length = -1;
    private transient int current = 0;

    @SuppressWarnings("LeakingThisInConstructor")
    public SpiralTask(Location location, int radius) {
        // limit is determined based on spiral leg length for given radius; see insideRadius()
        this.limit = (radius - 1) * 2;

        this.world = location.getWorld();
        if (this.world == null) {
            this.stop();
            return;
        }

        this.x = location.getChunk().getX();
        this.z = location.getChunk().getZ();

        this.readyToGo = true;

        // get this party started
        this.setTaskID(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SkyBlock.getInstance(), this, 2, 2));
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    /*
     * This is where the necessary work is done; you'll need to override this method with whatever you want
     * done at each chunk in the spiral pattern.
     * Return false if the entire task needs to be aborted, otherwise return true to continue.
     */
    public abstract boolean work();

    /*
     * Returns an FLocation pointing at the current chunk X and Z values.
     */
    public final FakeChunk currentChunk() {
        return new FakeChunk(world.getName(), x, z);
    }

    /*
     * Returns a Location pointing at the current chunk X and Z values.
     * note that the Location is at the corner of the chunk, not the center.
     */

    /*
     * Returns current chunk X and Z values.
     */
    public final int getX() {
        return x;
    }



    /*
     * Below are the guts of the class, which you normally wouldn't need to mess with.
     */

    public final int getZ() {
        return z;
    }

    public final void setTaskID(int ID) {
        if (ID == -1) {
            this.stop();
        }
        taskID = ID;
    }

    public final void run() {
        if (!this.valid() || !readyToGo) {
            return;
        }

        // this is set so it only does one iteration at a time, no matter how frequently the timer fires
        readyToGo = false;

        // make sure we're still inside the specified radius
        if (!this.insideRadius()) {
            return;
        }

        // track this to keep one iteration from dragging on too long and possibly choking the system
        long loopStartTime = now();

        // keep going until the task has been running for 20ms or more, then stop to take a breather
        while (now() < loopStartTime + 20) {
            // run the primary task on the current X/Z coordinates
            if (!this.work()) {
                this.finish();
                return;
            }

            // move on to next chunk in spiral
            if (!this.moveToNext()) {
                return;
            }
        }

        // ready for the next iteration to run
        readyToGo = true;
    }

    // step through chunks in spiral pattern from center; returns false if we're done, otherwise returns true
    public final boolean moveToNext() {
        if (!this.valid()) {
            return false;
        }

        // make sure we don't need to turn down the next leg of the spiral
        if (current < length) {
            current++;

            // if we're outside the radius, we're done
            if (!this.insideRadius()) {
                return false;
            }
        } else {    // one leg/side of the spiral down...
            current = 0;
            isZLeg ^= true;
            // every second leg (between X and Z legs, negative or positive), length increases
            if (isZLeg) {
                isNeg ^= true;
                length++;
            }
        }

        // move one chunk further in the appropriate direction
        if (isZLeg) {
            z += (isNeg) ? -1 : 1;
        } else {
            x += (isNeg) ? -1 : 1;
        }

        return true;
    }

    public final boolean insideRadius() {
        boolean inside = current < limit;
        if (!inside) {
            this.finish();
        }
        return inside;
    }

    // for successful completion
    public void finish() {
//		SaberFactions.plugin.log("SpiralTask successfully completed!");
        this.stop();
    }

    // we're done, whether finished or cancelled
    public final void stop() {
        if (!this.valid()) {
            return;
        }

        readyToGo = false;
        Bukkit.getServer().getScheduler().cancelTask(taskID);
        taskID = -1;
    }

    // is this task still valid/workable?
    public final boolean valid() {
        return taskID != -1;
    }

}