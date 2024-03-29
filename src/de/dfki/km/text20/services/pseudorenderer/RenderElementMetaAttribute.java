/*
 * RenderElementMetaAttribute.java
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
package de.dfki.km.text20.services.pseudorenderer;

/**
 * The status of a render element.
 * 
 * @author Ralf Biedert
 * @since 1.0
 */
public enum RenderElementMetaAttribute {
    /**
     * If JavaScript should be called back when gaze enters (BOOLEAN).
     */
    CALLBACK_ENTER_EXIT_GAZE,

    /**
     * If an element is set to invalid its presence should be ignored,
     * as it is not known whether the contained data is still valid (BOOLEAN).
     */
    INVALID,

    /**
     * Can be used to store arbitrary information which does not have an attribute
     * yet (Map<String, Serializable>).
     */
    MISC
}
