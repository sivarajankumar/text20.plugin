/*
 * TrackingDeviceImpl.java
 * 
 * Copyright (c) 2010, Ralf Biedert, DFKI. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA 02110-1301  USA
 *
 */
package de.dfki.km.text20.services.trackingdevices.eyes.impl.trackingserver;

import java.awt.Point;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import net.xeoh.plugins.base.PluginConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import net.xeoh.plugins.informationbroker.InformationBroker;
import net.xeoh.plugins.remote.RemoteAPILipe;
import de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingDevice;
import de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingDeviceInfo;
import de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingDeviceProvider;
import de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingDeviceType;
import de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingEventValidity;
import de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingListener;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingClientCallback;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingCommand;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingDeviceInformation;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingEvent;
import de.dfki.km.text20.trackingserver.eyes.remote.TrackingServerRegistry;
import de.dfki.km.text20.trackingserver.eyes.remote.options.SendCommandOption;

/**
 * 
 * @author rb
 * 
 */
@PluginImplementation
public class TrackingServerDeviceProviderImpl implements EyeTrackingDeviceProvider {

	private class ServerTrackingDevice implements EyeTrackingDevice,
			TrackingClientCallback {

		/** Can be set to recalibrate points from the tracker */
		public Point recalibration = new Point();

		/** */
		TrackingDeviceInformation deviceInformation;

		/** Indicates if the client is properly connected */
		final boolean isProperlyConnected;

		/** Manages acces to the listener. */
		final Lock listenerLock = new ReentrantLock();

		/** List of listeners we inform. */
		final List<EyeTrackingListener> trackingListener = new ArrayList<EyeTrackingListener>();

		/** */
		final TrackingServerRegistry registry;

		/** Last proper position values of the right eye */
		final LastObservations last = new LastObservations();

		/**
		 * @param string
		 * @throws URISyntaxException
		 * 
		 */
		public ServerTrackingDevice(final String string)
				throws URISyntaxException {
			// Get remote proxy of the server
			this.registry = TrackingServerDeviceProviderImpl.this.remoteAPI
					.getRemoteProxy(new URI(string),
							TrackingServerRegistry.class);

			TrackingServerDeviceProviderImpl.this.logger
					.info("Connected registry says " + this.registry);

			if (this.registry == null) {
				this.isProperlyConnected = false;
				return;
			}

			this.isProperlyConnected = true;

			TrackingServerDeviceProviderImpl.this.logger
					.fine("Obtaining device information. In case LipeRMI is still broken this may lock up ...");
			this.deviceInformation = this.registry
					.getTrackingDeviceInformation();
			TrackingServerDeviceProviderImpl.this.logger
					.fine("Device information obtained.");

			// final URI exportPlugin =
			// TrackingServerDeviceProviderImpl.this.remoteAPI.exportPlugin(this);
			// TrackingServerDeviceProviderImpl.this.logger.info("Our callback is exported to "
			// + exportPlugin.toString());

			this.registry.addTrackingListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingDevice#
		 * addTrackingListener
		 * (de.dfki.km.augmentedtext.services.trackingdevices.TrackingListener)
		 */
		public void addTrackingListener(final EyeTrackingListener listener) {
			if (!this.isProperlyConnected) {
				return;
			}
			this.listenerLock.lock();
			try {
				this.trackingListener.add(listener);
			} finally {
				this.listenerLock.unlock();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingDevice#
		 * getDeviceInfo()
		 */
		public EyeTrackingDeviceInfo getDeviceInfo() {
			return new EyeTrackingDeviceInfo() {

				public String getInfo(final String key) {
					if (ServerTrackingDevice.this.deviceInformation == null)
						return null;

					if (key.equals("DEVICE_NAME"))
						return ServerTrackingDevice.this.deviceInformation.deviceName;
					if (key.equals("DEVICE_MANUFACTURER"))
						return ServerTrackingDevice.this.deviceInformation.trackingDeviceManufacturer;
					if (key.equals("HARDWARE_ID"))
						return ServerTrackingDevice.this.deviceInformation.hardwareID;

					return null;
				}

				public String[] getKeys() {
					return new String[] { "DEVICE_NAME", "HARDWARE_ID",
							"DEVICE_MANUFACTURER" };
				}

				@SuppressWarnings("unused")
				public int getTrackingEventRate() {
					// TODO Auto-generated method stub
					return 0;
				}
			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingDevice#
		 * getDeviceType()
		 */
		public EyeTrackingDeviceType getDeviceType() {
			return EyeTrackingDeviceType.TRACKER;
		}

		/**
		 * @return the isProperlyConnected
		 */
		public boolean isProperlyConnected() {
			return this.isProperlyConnected;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.dfki.km.augmentedtext.trackingserver.remote.TrackingClientCallback
		 * #newTrackingEvent(de.dfki.km.augmentedtext.trackingserver.remote.
		 * TrackingEvent)
		 */
		public void newTrackingEvent(final TrackingEvent e) {
			// Sometimes null events might occur. Filter them.
			if (e == null)
				return;

			// Convert class to 'interface'
			final de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingEvent trackingEvent = new de.dfki.km.text20.services.trackingdevices.eyes.EyeTrackingEvent() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingEvent
				 * #areValid(de.dfki.km.augmentedtext.services.trackingdevices.
				 * TrackingEventValidity[])
				 */
				public boolean areValid(
						final EyeTrackingEventValidity... validities) {
					boolean rval = true;
					for (final EyeTrackingEventValidity v : validities) {
						if (v == EyeTrackingEventValidity.CENTER_POSITION_VALID) {
							rval &= e._centerValidity;
						}
					}
					return rval;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingEvent
				 * #eventTime()
				 */
				public long getEventTime() {
					return e.date;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingEvent
				 * #getGazeCenter()
				 */
				public Point getGazeCenter() {
					return new Point(e._centerX
							+ ServerTrackingDevice.this.recalibration.x,
							e._centerY
									+ ServerTrackingDevice.this.recalibration.y);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingEvent
				 * #getHeadPosition()
				 */
				public float[] getHeadPosition() {
					final float rval[] = new float[3];

					boolean leftEyeFound = true;
					boolean rightEyeFound = true;

					// First check if all of the head coordinates are okay.
					for (int i = 0; i < 3; i++) {

						// Update the backup value if the current value is in
						// valid range
						if (!(e.leftEyePos[i] > 0.0f && e.leftEyePos[i] < 1.0f)) {
							leftEyeFound = false;
						}

						if (!(e.rightEyePos[i] > 0.0f && e.rightEyePos[i] < 1.0f)) {
							rightEyeFound = false;
						}
					}

					// Check if we got both eyes ...
					if (leftEyeFound && rightEyeFound) {
						// If everything is fine, update the last deltas
						for (int i = 0; i < 3; i++) {
							ServerTrackingDevice.this.last.lastDeltas[i] = e.rightEyePos[i]
									- e.leftEyePos[i];
							ServerTrackingDevice.this.last.lastLeft[i] = e.leftEyePos[i];
							ServerTrackingDevice.this.last.lastRight[i] = e.rightEyePos[i];
						}

						ServerTrackingDevice.this.last.dateOfLeft = e.date;
						ServerTrackingDevice.this.last.dateOfRight = e.date;

						// Update rval
						for (int i = 0; i < 3; i++) {
							rval[i] = (e.leftEyePos[i] + e.rightEyePos[i]) / 2;
						}

						// And return
						return rval;
					}

					// In case we have only the left eye ....
					if (leftEyeFound) {
						// Update last deltas
						for (int i = 0; i < 3; i++) {
							ServerTrackingDevice.this.last.lastLeft[i] = e.leftEyePos[i];
						}

						ServerTrackingDevice.this.last.dateOfLeft = e.date;

						// Update rval
						for (int i = 0; i < 3; i++) {
							rval[i] = (e.leftEyePos[i] + ServerTrackingDevice.this.last.lastDeltas[i] / 2);
						}

						// And return
						return rval;
					}

					// Or the right eye ....
					if (rightEyeFound) {
						// Update last deltas
						for (int i = 0; i < 3; i++) {
							ServerTrackingDevice.this.last.lastRight[i] = e.rightEyePos[i];
						}

						ServerTrackingDevice.this.last.dateOfRight = e.date;

						// Update rval
						for (int i = 0; i < 3; i++) {
							rval[i] = (e.rightEyePos[i] - ServerTrackingDevice.this.last.lastDeltas[i] / 2);
						}

						// And return
						return rval;
					}

					// In this case, no eye was found, so we select the latest
					// position
					if (ServerTrackingDevice.this.last.dateOfLeft > ServerTrackingDevice.this.last.dateOfRight) {
						for (int i = 0; i < 3; i++) {
							rval[i] = (ServerTrackingDevice.this.last.lastLeft[i] - ServerTrackingDevice.this.last.lastDeltas[i] / 2);
						}

					} else {
						for (int i = 0; i < 3; i++) {
							rval[i] = (ServerTrackingDevice.this.last.lastRight[i] + ServerTrackingDevice.this.last.lastDeltas[i] / 2);
						}
					}

					return rval;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Object#toString()
				 */
				public float getLeftEyeDistance() {
					return e.eyeDistances[0];
				}

				public float[] getLeftEyePosition() {
					return e.leftEyePos;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingEvent
				 * #pupilSizeLeft()
				 */
				public float getPupilSizeLeft() {
					return e.pupilSizeLeft;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingEvent
				 * #pupilSizeRight()
				 */
				public float getPupilSizeRight() {
					return e.pupilSizeRight;
				}

				public float getRightEyeDistance() {
					return e.eyeDistances[1];
				}

				public float[] getRightEyePosition() {
					return e.rightEyePos;
				}

				@Override
				public String toString() {
					final StringBuilder sb = new StringBuilder();
					sb.append("[");
					sb.append(getEventTime());
					sb.append(": ");
					sb.append(getGazeCenter());
					sb.append("]");
					return sb.toString();
				}

				public Point getLeftEyeGazePoint() {
					return e.leftGaze;
				}

				public float[] getLeftEyeGazePosition() {
					return e.gazeLeftPos;
				}

				public Point getRightEyeGazePoint() {
					return e.rightGaze;
				}

				public float[] getRightEyeGazePosition() {
					return e.gazeRightPos;
				}
			};

			// Lock listeners ...
			ServerTrackingDevice.this.listenerLock.lock();

			// TODO: Decouple the follwing calls from this method, otherwise we
			// might block our caller ...

			try {
				// And dispatch it to the listener
				for (int i = 0; i < ServerTrackingDevice.this.trackingListener
						.size(); i++) {
					final EyeTrackingListener l = ServerTrackingDevice.this.trackingListener
							.get(i);
					l.newTrackingEvent(trackingEvent);
				}
			} finally {
				ServerTrackingDevice.this.listenerLock.unlock();
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingDevice#
		 * sendCommand
		 * (de.dfki.km.augmentedtext.trackingserver.remote.TrackingCommand,
		 * de.dfki
		 * .km.augmentedtext.trackingserver.remote.options.SendCommandOption[])
		 */
		public void sendLowLevelCommand(TrackingCommand command,
				SendCommandOption... options) {
			this.registry.sendCommand(command, options);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingDevice#
		 * closeDevice()
		 */
		public void closeDevice() {
			// TODO Auto-generated method stub

		}
	}

	/** */
	@InjectPlugin
	public PluginConfiguration configuration;

	/** */
	@InjectPlugin
	public InformationBroker infobroker;

	/** */
	@InjectPlugin
	public RemoteAPILipe remoteAPI;

	/** */
	final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Return what we can do...
	 * 
	 * @return .
	 */
	@Capabilities
	public String[] getCapabilities() {
		return new String[] { "eyetrackingdevice:trackingserver" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.dfki.km.augmentedtext.services.trackingdevices.TrackingDeviceProvider
	 * #openDevice(java.lang.String)
	 */
	public EyeTrackingDevice openDevice(final String url) {
		try {
			ServerTrackingDevice serverTrackingDevice = new ServerTrackingDevice(
					url);
			if (serverTrackingDevice.isProperlyConnected())
				return serverTrackingDevice;

			return null;

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

}