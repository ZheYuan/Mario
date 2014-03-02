/**
 *    Copyright 2014 Renren.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.renren.Wario.zookeeper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ZooKeeperCluster {
	private static Logger logger = LogManager.getLogger(ZooKeeperCluster.class
			.getName());
	
	private JSONObject object = null;
	//connectStrings 为null会在update的时候出现NullPointException
	private Set<String> connectStrings = new HashSet<String>();
	private int sessionTimeout = 10000;
	private final String zookeeperName;

	private Map<String, ZooKeeperClient> clients = null;

	/**
	 * @return the clients
	 */
	public Map<String, ZooKeeperClient> getClients() {
		return clients;
	}

	public ZooKeeperCluster(String zookeeperName, JSONObject object) {
		this.zookeeperName = zookeeperName;
		this.object = object;
	}

	/**
	 * @return the zookeeperName
	 */
	public String getZookeeperName() {
		return zookeeperName;
	}

	public void init() throws JSONException {
		clients = new HashMap<String, ZooKeeperClient>();
		connectStrings = readJSONObject();
		addClients(connectStrings);
	}

	public void updateClients(JSONObject object) throws JSONException {
		this.object = object;
		Set<String> newConnectStrings = readJSONObject();
		Set<String> tmp = getIntersection(connectStrings, newConnectStrings);
		deleteClients(getDifference(connectStrings, tmp));
		addClients(getDifference(newConnectStrings, tmp));
		connectStrings = newConnectStrings;
	}

	@SuppressWarnings("unused")
	private Set<String> getUnion(Set<String> a, Set<String> b) {
		Set<String> res = new HashSet<String>();
		res.clear();
		res.addAll(a);
		res.addAll(b);
		return res;
	}

	private Set<String> getDifference(Set<String> a, Set<String> b) {
		Set<String> res = new HashSet<String>();
		res.clear();
		res.addAll(a);
		res.removeAll(b);
		return res;
	}

	private Set<String> getIntersection(Set<String> a, Set<String> b) {
		Set<String> res = new HashSet<String>();
		res.clear();
		res.addAll(a);
		res.retainAll(b);
		return res;
	}

	private void addClients(Set<String> connectStrings) {
		Iterator<String> it = connectStrings.iterator();
		while (it.hasNext()) {
			String connectString = it.next();
			if (!clients.containsKey(connectString)) {
				AddClient add = new AddClient(connectString, sessionTimeout);
				new Thread(add).start();
			}
		}
	}

	private class AddClient implements Runnable {

		private String connectString = null;
		private int sessionTimeout;

		public AddClient(String connectString, int sessionTimeout) {
			this.connectString = connectString;
			this.sessionTimeout = sessionTimeout;
		}

		@Override
		public void run() {
			ZooKeeperClient client = new ZooKeeperClient(connectString,
					sessionTimeout);
			synchronized (clients) {
				clients.put(connectString, client);
			}
			client.createConnection();
		}

	}

	private void deleteClients(Set<String> connectStrings) {
		Iterator<String> it = connectStrings.iterator();
		while (it.hasNext()) {
			String connectString = it.next();
			if (clients.containsKey(connectString)) {
				ZooKeeperClient zookeeperClient = clients.get(connectString);
				zookeeperClient.releaseConnection();
				clients.remove(connectString);
			}
		}
	}

	private Set<String> readJSONObject() throws JSONException {
		Set<String> res = new HashSet<String>();
		try {
			JSONArray connectStringArray = object.getJSONArray("serverIPList");
			for (int i = 0; i < connectStringArray.length(); ++i) {
				res.add(connectStringArray.getString(i));
			}
			sessionTimeout = object.getInt("sessionTimeout");
		} catch (JSONException e) {
			logger.error("Json format error : " + object.toString() + "\n" + e.toString());
			throw e;
		}
		return res;
	}

}
