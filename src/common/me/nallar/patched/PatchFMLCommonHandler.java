package me.nallar.patched;

import java.util.EnumSet;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.profiler.Profiler;

public abstract class PatchFMLCommonHandler extends FMLCommonHandler {
	public Profiler theProfiler = null;

	@Override
	public void rescheduleTicks(Side side) {
		List<IScheduledTickHandler> scheduledTicks = side.isClient() ? scheduledClientTicks : scheduledServerTicks;
		synchronized (scheduledTicks) {
			TickRegistry.updateTickQueue(scheduledTicks, side);
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> ticks, Side side, Object... data) {
		if (theProfiler == null) {
			theProfiler = getMinecraftServerInstance().theProfiler;
		}
		final List<IScheduledTickHandler> scheduledTicks = side.isClient() ? scheduledClientTicks : scheduledServerTicks;

		synchronized (scheduledTicks) {
			if (scheduledTicks.isEmpty()) {
				return;
			}
			for (IScheduledTickHandler ticker : scheduledTicks) {
				EnumSet<TickType> ticksToRun = ticker.ticks();
				if (ticksToRun == null) {
					ticksToRun = EnumSet.noneOf(TickType.class);
				} else {
					ticksToRun = EnumSet.copyOf(ticksToRun);
				}
				ticksToRun.retainAll(ticks);
				if (!ticksToRun.isEmpty()) {
					theProfiler.startSection(ticker.getClass().toString());
					ticker.tickStart(ticksToRun, data);
					theProfiler.endSection();
				}
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> ticks, Side side, Object... data) {
		final List<IScheduledTickHandler> scheduledTicks = side.isClient() ? scheduledClientTicks : scheduledServerTicks;

		synchronized (scheduledTicks) {
			if (scheduledTicks.isEmpty()) {
				return;
			}
			for (IScheduledTickHandler ticker : scheduledTicks) {
				EnumSet<TickType> ticksToRun = ticker.ticks();
				if (ticksToRun == null) {
					ticksToRun = EnumSet.noneOf(TickType.class);
				} else {
					ticksToRun = EnumSet.copyOf(ticksToRun);
				}
				ticksToRun.retainAll(ticks);
				if (!ticksToRun.isEmpty()) {
					theProfiler.startSection(ticker.getClass().toString());
					ticker.tickEnd(ticksToRun, data);
					theProfiler.endSection();
				}
			}
		}
	}
}
