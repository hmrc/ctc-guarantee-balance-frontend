package pages

import pages.behaviours.PageBehaviours


class GuaranteeReferenceNumberPageSpec extends PageBehaviours {

  "GuaranteeReferenceNumberPage" - {

    beRetrievable[String](GuaranteeReferenceNumberPage)

    beSettable[String](GuaranteeReferenceNumberPage)

    beRemovable[String](GuaranteeReferenceNumberPage)
  }
}
