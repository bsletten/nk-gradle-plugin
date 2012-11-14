package net.bosatsu.util.netkernel

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.bosatsu.util.netkernel.AppositeHelper

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore
import org.junit.Test
import org.mortbay.jetty.Request
import org.mortbay.jetty.Server
import org.mortbay.jetty.handler.AbstractHandler;

class AppositeHelperTest {


   static int PORT = 12000
   static Server server
   static def MOCK_RESPONSES = [:]

   AppositeHelper appositeHelper = new AppositeHelper("http://localhost:$PORT")

   @BeforeClass
   static void beforeClass() {

      MOCK_RESPONSES["/tools/apposite/unattended/v1/synchronize"] = "Apposite Sychronized Successfully"
      MOCK_RESPONSES["/tools/apposite/unattended/v1/installed?match=installedPackage"] = '''
      <resultset xmlns:hds="http://netkernel.org/hds">
        <row>
          <ID hds:type="INTEGER">130</ID>
          <RUNLEVEL hds:type="INTEGER">5</RUNLEVEL>
          <NAME>installedPackage</NAME>
          <INSTALLED hds:type="BOOLEAN">true</INSTALLED>
          <HASSECURITY/>
          <HASUPDATE hds:type="BOOLEAN">true</HASUPDATE>
          <VERSIONID hds:type="INTEGER">173</VERSIONID>
          <VP>packageVersion</VP>
        </row>
      </resultset>
      '''
      MOCK_RESPONSES["/tools/apposite/unattended/v1/installed?match=uninstalledPackage"] = '<resultset xmlns:hds="http://netkernel.org/hds"/>'
      MOCK_RESPONSES["/tools/apposite/unattended/v1/change?update=package"] = ""
      MOCK_RESPONSES["/tools/apposite/unattended/v1/change?update=package"] = ""
      MOCK_RESPONSES["/tools/apposite/unattended/v1/installed?match=installedPackageWithMultipleMatches"] = '''
      <resultset xmlns:hds="http://netkernel.org/hds">
        <row>
          <ID hds:type="INTEGER">130</ID>
          <RUNLEVEL hds:type="INTEGER">5</RUNLEVEL>
          <NAME>installedPackageWithMultipleMatches</NAME>
          <INSTALLED hds:type="BOOLEAN">true</INSTALLED>
          <HASSECURITY/>
          <HASUPDATE hds:type="BOOLEAN">true</HASUPDATE>
          <VERSIONID hds:type="INTEGER">173</VERSIONID>
          <VP>packageVersion</VP>
        </row>
        <row>
          <ID hds:type="INTEGER">130</ID>
          <RUNLEVEL hds:type="INTEGER">5</RUNLEVEL>
          <NAME>installedPackageWithMultipleMatchesExtra</NAME>
          <INSTALLED hds:type="BOOLEAN">true</INSTALLED>
          <HASSECURITY/>
          <HASUPDATE hds:type="BOOLEAN">true</HASUPDATE>
          <VERSIONID hds:type="INTEGER">173</VERSIONID>
          <VP>packageVersion</VP>
        </row>
      </resultset>
            '''
      MOCK_RESPONSES["/tools/apposite/unattended/v1/installed?match=installed"] = '''
      <resultset xmlns:hds="http://netkernel.org/hds">
        <row>
          <ID hds:type="INTEGER">130</ID>
          <RUNLEVEL hds:type="INTEGER">5</RUNLEVEL>
          <NAME>installedPackage</NAME>
          <INSTALLED hds:type="BOOLEAN">true</INSTALLED>
          <HASSECURITY/>
          <HASUPDATE hds:type="BOOLEAN">true</HASUPDATE>
          <VERSIONID hds:type="INTEGER">173</VERSIONID>
          <VP>packageVersion</VP>
        </row>
      </resultset>
            '''
      server = new Server(PORT)
      server.addHandler(new AbstractHandler() {
               public void handle(String target, HttpServletRequest request,
               HttpServletResponse response, int dispatch) throws IOException,
               ServletException {
                  def responseBody = MOCK_RESPONSES[request.uri.toString()]
                  response.writer.println(responseBody)
                  response.writer.flush()
                  ((Request) request).setHandled(true)
               }
            })
      server.start()
   }

   @AfterClass
   static void afterClass() {
      server.stop()
   }

   @Test
   void synchronizeSuccess() {
      appositeHelper.synchronize()
   }

   @Test
   void isInstalledSuccess() {
      Assert.assertTrue appositeHelper.isInstalled("installedPackage", "packageVersion")
   }

   @Test
   void installSuccess() {
      appositeHelper.install("uninstalledPackage", "packageVersion", 1, 0)
   }

   @Test
   void updateSuccess() {
      appositeHelper.update("installedPackage", "packageVersion", 1, 0)
   }

   @Test
   void isInstalledSuccessWithMultipleModules() {
      Assert.assertTrue appositeHelper.isInstalled("installedPackageWithMultipleMatches", "packageVersion")
   }

   @Test
   void isInstalledFailsPackageNameNotFound() {
      Assert.assertFalse appositeHelper.isInstalled("installed", "packageVersion")
   }
   
   @Test
   void isInstalledFailsPackageVersionDoesntMatch() {
      Assert.assertFalse appositeHelper.isInstalled("installed", "wrongPackageVersion")
   }
}