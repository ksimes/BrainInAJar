/**************************************************************************/
/*
/* UnixSocketImpl.java -- Part of the org.lirc.socket package
/* Copyright (C) 2001 Bjorn Bringert (bjorn@mumblebee.com)
/*
/* This program is free software; you can redistribute it and/or
/* modify it under the terms of the GNU General Public License
/* as published by the Free Software Foundation; either version 2
/* of the License, or (at your option) any later version.
/*
/* This program is distributed in the hope that it will be useful,
/* but WITHOUT ANY WARRANTY; without even the implied warranty of
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/* GNU General Public License for more details.
/*
/* You should have received a copy of the GNU General Public License
/* along with this program; if not, write to the Free Software
/* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
/*
/**************************************************************************/

package org.lirc.socket;

import java.io.*;

/**
 * Implementation for both server and client sockets.
 *
 * @version $Revision: 1.1 $
 * @author Bjorn Bringert (bjorn@mumblebee.com)
 */
public class UnixSocketImpl {

	static {
		// Loads the JNI library from the directory of this class
		String libname = System.mapLibraryName("junixsocket");
		// uses getFile() instead of getPath() as getPath() is not available in JDK1.2.2
//		String libpath = UnixSocketImpl.class.getResource(libname).getFile();
		String libpath = UnixSocketImpl.class.getResource(libname).getPath();
		System.load(libpath);
	}

	/**
	 * The file descriptor for the socket.
	 */
	private int fd = -1;

	/**
	 * Creates a new UnixSocketImpl
	 * @param fd a socket file descriptor
	 */
	private UnixSocketImpl(int fd) {
		this.fd = fd;
	}

	/**
	 * Creates a new UnixSocketImpl
	 */
	protected UnixSocketImpl() throws IOException {
		this.fd = createSocket();
	}

	/**
	 * Gets the file descriptor for the socket.
	 * @return a file descriptor, or -1 if there is none
	 */
	protected int getFd() {
		return fd;
	}

	/**
	 * Creates a native socket and returns its file descriptor.
	 * @throws IOException if an I/O error occurs when creating
	 * the socket.
	 */
	protected static native int createSocket() throws IOException;

	/**
	 * Binds to a path
	 * @param myPath path to bind to.
	 * @throws IOException if an I/O error occurs when binding.
	 */
	protected native void bind(String myPath) throws IOException;

	/**
	 * Starts listening.
	 * @param backlog The maximum length of the queue
	 * @throws IOException if an I/O error occurs.
	 */
	protected native void listen(int backlog) throws IOException;

	/**
	* Listens for a connection to be made to this socket and accepts
	* it. The method blocks until a connection is made.
	* @throws IOException if an I/O error occurs when waiting for a
	*               connection.
	* @return the new UnixSocketImpl
	*/
	protected UnixSocketImpl accept() throws IOException {
		return new UnixSocketImpl(nativeAccept());
	}

	/**
	* Listens for a connection to be made to this socket and accepts
	* it. The method blocks until a connection is made.
	* @throws IOException if an I/O error occurs when waiting for a
	*               connection.
	* @return the file descriptor for the new socket
	*/
	private native int nativeAccept() throws IOException;

	/**
	 * Connects this socket to a path.
	 * @throws If there is a problem connecting the socket
	 */
	protected native void connect(String path) throws IOException;

	/**
	* Closes this socket
	* @throws IOException If there is a problem closing the socket
	*/
	protected native void close() throws IOException;

	/**
	* Disables the input stream for this socket.
	*
	* @throws IOException if an I/O error occurs when shutting down this
	* socket.
	*/
	protected native void shutdownInput() throws IOException;

	/**
	* Disables the output stream for this socket.
	*
	* @throws IOException if an I/O error occurs when shutting down this
	* socket.
	*/
	protected native void shutdownOutput() throws IOException;

	/**
	* Returns an input stream for this socket. This method should only
	* be called once for a socket.
	* @return an input stream for reading bytes from this socket.
	* @throws IOException if an I/O error occurs when creating the
	* input stream.
	*/
	protected UnixSocketInputStream getInputStream() throws IOException {
		return new UnixSocketInputStream(this);
	}

	/**
	* Returns an output stream for this socket. This method should only
	* be called once for a socket.
	* @return an output stream for writing bytes to this socket.
	* @throws IOException if an I/O error occurs when creating the
	* output stream.
	*/
	protected UnixSocketOutputStream getOutputStream() throws IOException {
		return new UnixSocketOutputStream(this);
	}

}