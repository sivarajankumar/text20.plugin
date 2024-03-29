/*
 * EvaluationEvent.java
 * 
 * Copyright (c) 2010, Andre Hoffmann, DFKI. All rights reserved.
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
package de.dfki.km.text20.services.evaluators.common;

/**
 * Base class for an evaluation event passed to the {@link EvaluationListener}. 
 * 
 * @author Ralf Biedert
 * @since 1.3
 */
public interface EvaluationEvent {

    /**
     * Returns the time this event was generated.
     * 
     * @return The time the event was generated.
     */
    public long getGenerationTime();
}
