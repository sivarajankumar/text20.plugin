/*
 * Saccade.java
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
package de.dfki.km.text20.services.evaluators.gaze.listenertypes.saccade;

import de.dfki.km.text20.services.evaluators.gaze.listenertypes.fixation.Fixation;
import de.dfki.km.text20.services.evaluators.gaze.listenertypes.saccade.util.SaccadeUtil;

/**
 * Reflects a saccade, the <i>jump</i> between two fixations, which will be contained in a {@link SaccadeEvent}.
 * 
 * @author Ralf Biedert
 * @since 1.0
 * @see SaccadeUtil
 */
public interface Saccade {

    /**
     * Returns the end fixation.
     * 
     * @return The end fixation.
     */
    public Fixation getEnd();

    /**
     * Returns the start fixation.
     * 
     * @return The start fixation.
     */
    public Fixation getStart();
}
