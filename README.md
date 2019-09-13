# bachelor-thesis
This is a bachelor thesis written in 2019 for BSc Informatik, Goethe-Universität Frankfurt am Main

# Abstract

This thesis aims at an explorative implementation of a real-time learning analytics engine based
on Kafka and Kafka Streams. A real-time learning analytics engine should be able to make re-
sults of analytics run on data by a multitude of sources available near the time of the data’s
arrival in the system. Amongst these sources may be traditional relational databases as well
as streams of data from sensors or micro-controllers. Such a learning analytics engine would
allow for delivering observations and indications to actors in the learning environment whilst
the relevant action itself is happening, enabling a fluent interaction of the system and its en-
vironment. This thesis is concerned with building such a system. It consists of two parts, a
conceptual and an implementation part. In the conceptual part, the κ-Architecture is under-
stood as a derivation of the λ-Architecture. While requirements defined by aspects of big data
as well as devops may be well met by the κ-Architecture, doubts regarding its use for a learning
analytics engine are articulated. Given the Log-based conception of Kafka, problems of com-
pliance with the GDPR are articulated, though not addressed further. Next, some conventions
for the system are suggested and discussed, including naming patterns, data structures and
data formats. This prepares the thesis’s second part, the implementation. A dockerized, hori-
zontally scalable system based on Kafka and Kafka Streams applications will be implemented.
The system processes two exemplary, heterogeneous sources of data, which differ with regard
to the data’s velocity and to their processors’ complexity: Relations from a Moodle’s MySQL
database represent a first class of data, frames from a leap motion sensor represent a second
class. The results, some simple statistical evaluations made possible by joins and aggregations
of several streams, will be presented in a continuous view.
