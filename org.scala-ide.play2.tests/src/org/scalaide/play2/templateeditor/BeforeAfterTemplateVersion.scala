package org.scalaide.play2.templateeditor

import org.junit.After
import org.junit.Before
import org.scalaide.play2.templateeditor.processing.TemplateVersionExhibitor

trait BeforeAfterTemplateVersion {
  @Before def setUp(): Unit = {
    TemplateVersionExhibitor.set(Some("2.4"))
  }

  @After def tearDown(): Unit = {
    TemplateVersionExhibitor.clean()
  }
}
