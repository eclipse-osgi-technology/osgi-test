/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package org.osgi.test.assertj.cm.configurationevent;

import static org.osgi.service.cm.ConfigurationEvent.CM_DELETED;
import static org.osgi.service.cm.ConfigurationEvent.CM_LOCATION_CHANGED;
import static org.osgi.service.cm.ConfigurationEvent.CM_UPDATED;

import org.osgi.test.common.bitmaps.Bitmap;

public class ConfigurationEventType {

	private static final int[] TYPES = {
		CM_DELETED, CM_LOCATION_CHANGED, CM_UPDATED
	};

	public static String toString(int type) {
		switch (type) {
			case CM_UPDATED :
				return "CM_UPDATED";
			case CM_DELETED :
				return "CM_DELETED";
			case CM_LOCATION_CHANGED :
				return "CM_LOCATION_CHANGED";
			default :
				return Integer.toString(type);
		}
	}

	public static final Bitmap BITMAP = new Bitmap(TYPES, ConfigurationEventType::toString);
}
